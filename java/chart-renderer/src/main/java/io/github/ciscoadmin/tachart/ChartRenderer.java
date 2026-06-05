package io.github.ciscoadmin.tachart;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public final class ChartRenderer {
    private static final Color PASSED_COLOR = new Color(0x8A, 0xD6, 0x8B);
    private static final Color FAILED_COLOR = new Color(0xFF, 0x5A, 0x5A);
    private static final Color EMPTY_COLOR = new Color(0xDD, 0xDD, 0xDD);
    private static final Color TEXT_COLOR = new Color(0x11, 0x11, 0x11);
    private static final Color LEGEND_TEXT_COLOR = new Color(0x66, 0x66, 0x66);
    private static final Color BACKGROUND_COLOR = Color.WHITE;

    private ChartRenderer() {
    }

    public static void main(String[] args) {
        if (args.length == 0 || hasFlag(args, "--help") || hasFlag(args, "-h")) {
            printUsage();
            return;
        }

        try {
            Options options = Options.from(parseArgs(args));
            render(options);
            System.out.println("Chart written to " + options.output.toAbsolutePath());
        } catch (IllegalArgumentException ex) {
            System.err.println("Error: " + ex.getMessage());
            System.err.println("Use --help to show usage.");
            System.exit(2);
        } catch (IOException ex) {
            System.err.println("Error: unable to write chart image: " + ex.getMessage());
            System.exit(1);
        }
    }

    private static void render(Options options) throws IOException {
        int pixelWidth = Math.round(options.width * options.scale);
        int pixelHeight = Math.round(options.height * options.scale);
        BufferedImage image = new BufferedImage(pixelWidth, pixelHeight, BufferedImage.TYPE_INT_RGB);

        Graphics2D graphics = image.createGraphics();
        try {
            graphics.scale(options.scale, options.scale);
            applyQualityHints(graphics);
            drawChart(graphics, options);
        } finally {
            graphics.dispose();
        }

        Path parent = options.output.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        ImageIO.write(image, "png", options.output.toFile());
    }

    private static void drawChart(Graphics2D graphics, Options options) {
        graphics.setColor(BACKGROUND_COLOR);
        graphics.fillRect(0, 0, options.width, options.height);

        int titleHeight = drawTitle(graphics, options);

        int legendX = Math.round(options.width * 0.73f);
        int availableChartWidth = legendX - 12;
        int chartTop = titleHeight == 0 ? 0 : titleHeight + 4;
        int availableChartHeight = options.height - chartTop;
        int centerX = availableChartWidth / 2;
        int centerY = chartTop + availableChartHeight / 2;
        int outerRadius = Math.max(30, Math.min(availableChartWidth, availableChartHeight) / 2 - 2);
        int innerRadius = Math.round(outerRadius * options.cutout);

        if (options.total() == 0) {
            drawEmptyChart(graphics, centerX, centerY, outerRadius, innerRadius, legendX);
            return;
        }

        drawDoughnut(graphics, centerX, centerY, outerRadius, innerRadius, options);
        drawCenterTotal(graphics, centerX, centerY, outerRadius, options.total());
        drawFailedPercent(graphics, centerX, centerY, outerRadius, innerRadius, legendX, titleHeight, options);
        drawLegend(graphics, legendX, centerY, options);
    }

    private static int drawTitle(Graphics2D graphics, Options options) {
        if (options.title == null || options.title.isBlank()) {
            return 0;
        }

        int fontSize = Math.max(13, Math.round(options.height * 0.055f));
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
        graphics.setColor(LEGEND_TEXT_COLOR);

        FontMetrics metrics = graphics.getFontMetrics();
        String title = trimToFit(graphics, options.title, options.width - 24);
        int x = (options.width - metrics.stringWidth(title)) / 2;
        int baseline = 10 + metrics.getAscent();
        graphics.drawString(title, x, baseline);
        return baseline + metrics.getDescent() + 4;
    }

    private static void drawDoughnut(Graphics2D graphics, int centerX, int centerY, int outerRadius, int innerRadius, Options options) {
        double total = options.total();
        double failedAngle = total == 0 ? 0 : options.failed * 360.0 / total;
        double passedAngle = 360.0 - failedAngle;

        if (options.failed == 0 || failedAngle <= 0.0) {
            fillRing(graphics, centerX, centerY, outerRadius, innerRadius, PASSED_COLOR);
            return;
        }
        if (options.passed == 0 || passedAngle <= 0.0) {
            fillRing(graphics, centerX, centerY, outerRadius, innerRadius, FAILED_COLOR);
            return;
        }

        Shape passedSegment = ringSegment(centerX, centerY, outerRadius, innerRadius, 90.0, passedAngle);
        Shape failedSegment = ringSegment(centerX, centerY, outerRadius, innerRadius, 90.0 - passedAngle, failedAngle);

        fillAndOutline(graphics, passedSegment, PASSED_COLOR);
        fillAndOutline(graphics, failedSegment, FAILED_COLOR);
    }

    private static void fillRing(Graphics2D graphics, int centerX, int centerY, int outerRadius, int innerRadius, Color color) {
        Area area = new Area(new Ellipse2D.Double(centerX - outerRadius, centerY - outerRadius, outerRadius * 2.0, outerRadius * 2.0));
        area.subtract(new Area(new Ellipse2D.Double(centerX - innerRadius, centerY - innerRadius, innerRadius * 2.0, innerRadius * 2.0)));
        fillAndOutline(graphics, area, color);
    }

    private static void fillAndOutline(Graphics2D graphics, Shape shape, Color color) {
        graphics.setColor(color);
        graphics.fill(shape);
        graphics.setStroke(new BasicStroke(2.0f));
        graphics.setColor(BACKGROUND_COLOR);
        graphics.draw(shape);
    }

    private static Shape ringSegment(int centerX, int centerY, int outerRadius, int innerRadius, double startDeg, double clockwiseSweepDeg) {
        int steps = Math.max(8, (int) Math.ceil(clockwiseSweepDeg / 2.0));
        Path2D.Double path = new Path2D.Double();

        for (int index = 0; index <= steps; index++) {
            double angle = startDeg - clockwiseSweepDeg * index / steps;
            double radians = Math.toRadians(angle);
            double x = centerX + outerRadius * Math.cos(radians);
            double y = centerY - outerRadius * Math.sin(radians);
            if (index == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }

        for (int index = steps; index >= 0; index--) {
            double angle = startDeg - clockwiseSweepDeg * index / steps;
            double radians = Math.toRadians(angle);
            double x = centerX + innerRadius * Math.cos(radians);
            double y = centerY - innerRadius * Math.sin(radians);
            path.lineTo(x, y);
        }

        path.closePath();
        return path;
    }

    private static void drawCenterTotal(Graphics2D graphics, int centerX, int centerY, int outerRadius, int total) {
        int fontSize = Math.max(32, Math.round(outerRadius * 0.36f));
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
        graphics.setColor(Color.BLACK);

        String value = Integer.toString(total);
        FontMetrics metrics = graphics.getFontMetrics();
        int x = centerX - metrics.stringWidth(value) / 2;
        int y = centerY + (metrics.getAscent() - metrics.getDescent()) / 2;
        graphics.drawString(value, x, y);
    }

    private static void drawEmptyChart(Graphics2D graphics, int centerX, int centerY, int outerRadius, int innerRadius, int legendX) {
        fillRing(graphics, centerX, centerY, outerRadius, innerRadius, EMPTY_COLOR);
        drawCenterTotal(graphics, centerX, centerY - Math.round(outerRadius * 0.06f), outerRadius, 0);

        int fontSize = Math.max(13, Math.round(outerRadius * 0.075f));
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
        graphics.setColor(LEGEND_TEXT_COLOR);

        String label = "No test data";
        FontMetrics metrics = graphics.getFontMetrics();
        int x = centerX - metrics.stringWidth(label) / 2;
        int y = centerY + Math.round(outerRadius * 0.22f);
        graphics.drawString(label, x, y);

        drawEmptyLegend(graphics, legendX, centerY);
    }

    private static void drawFailedPercent(Graphics2D graphics, int centerX, int centerY, int outerRadius, int innerRadius, int legendX, int titleHeight, Options options) {
        if (options.failed <= 0 || options.passed <= 0 || options.total() <= 0) {
            return;
        }

        double failedAngle = options.failed * 360.0 / options.total();
        if (failedAngle < 4.0) {
            return;
        }

        double passedAngle = 360.0 - failedAngle;
        double midAngle = 90.0 - passedAngle - failedAngle / 2.0;
        double radians = Math.toRadians(midAngle);
        double labelRadius = innerRadius + (outerRadius - innerRadius) * 0.50;
        int x = (int) Math.round(centerX + labelRadius * Math.cos(radians));
        int y = (int) Math.round(centerY - labelRadius * Math.sin(radians));

        String text = Math.round(options.failed * 100.0 / options.total()) + "%";
        int fontSize = Math.max(14, Math.round(outerRadius * 0.135f));
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
        graphics.setColor(TEXT_COLOR);

        FontMetrics metrics = graphics.getFontMetrics();
        if (labelFitsInsideSector(metrics, text, labelRadius, failedAngle, outerRadius, innerRadius)) {
            drawCenteredText(graphics, text, x, y, metrics);
            return;
        }

        drawOutsideFailedPercent(graphics, text, metrics, centerX, centerY, outerRadius, legendX, titleHeight, options, radians);
    }

    private static boolean labelFitsInsideSector(FontMetrics metrics, String text, double labelRadius, double angleDegrees, int outerRadius, int innerRadius) {
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getAscent() + metrics.getDescent();
        double availableArcLength = outerRadius * Math.toRadians(angleDegrees);
        int ringThickness = outerRadius - innerRadius;

        return textWidth + 2 <= availableArcLength && textHeight + 4 <= ringThickness;
    }

    private static void drawOutsideFailedPercent(
            Graphics2D graphics,
            String text,
            FontMetrics metrics,
            int centerX,
            int centerY,
            int outerRadius,
            int legendX,
            int titleHeight,
            Options options,
            double radians
    ) {
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getAscent() + metrics.getDescent();
        int labelRadius = outerRadius + 24;

        int rawX = (int) Math.round(centerX + labelRadius * Math.cos(radians));
        int rawY = (int) Math.round(centerY - labelRadius * Math.sin(radians));

        int minCenterX = 4 + textWidth / 2;
        int maxCenterX = Math.max(minCenterX, legendX - 12 - textWidth / 2);
        int minCenterY = Math.max(4 + textHeight / 2, titleHeight + 4 + textHeight / 2);
        int maxCenterY = Math.max(minCenterY, options.height - 4 - textHeight / 2);
        int labelCenterX = clamp(rawX, minCenterX, maxCenterX);
        int labelCenterY = clamp(rawY, minCenterY, maxCenterY);

        double anchorX = centerX + outerRadius * Math.cos(radians);
        double anchorY = centerY - outerRadius * Math.sin(radians);
        int horizontalDirection = Math.cos(radians) < 0 ? -1 : 1;
        if (rawY < minCenterY || rawY > maxCenterY) {
            int sideX = (int) Math.round(anchorX + horizontalDirection * (textWidth / 2.0 + 16));
            labelCenterX = clamp(sideX, minCenterX, maxCenterX);
        }
        labelCenterX = pushOutsideRing(centerX, centerY, labelCenterX, labelCenterY, textWidth, outerRadius, minCenterX, maxCenterX, horizontalDirection);

        double elbowX = centerX + (outerRadius + 10) * Math.cos(radians);
        double elbowY = centerY - (outerRadius + 10) * Math.sin(radians);
        int lineEndX = labelCenterX < anchorX ? labelCenterX + textWidth / 2 + 3 : labelCenterX - textWidth / 2 - 3;

        graphics.setStroke(new BasicStroke(1.4f));
        graphics.setColor(new Color(0x99, 0x99, 0x99));
        Path2D.Double leader = new Path2D.Double();
        leader.moveTo(anchorX, anchorY);
        leader.lineTo(elbowX, elbowY);
        leader.lineTo(lineEndX, labelCenterY);
        graphics.draw(leader);

        graphics.setColor(TEXT_COLOR);
        drawPercentBadge(graphics, text, labelCenterX, labelCenterY, metrics);
    }

    private static void drawCenteredText(Graphics2D graphics, String text, int centerX, int centerY, FontMetrics metrics) {
        graphics.drawString(text, centerX - metrics.stringWidth(text) / 2, centerY + (metrics.getAscent() - metrics.getDescent()) / 2);
    }

    private static void drawPercentBadge(Graphics2D graphics, String text, int centerX, int centerY, FontMetrics metrics) {
        int horizontalPadding = 7;
        int verticalPadding = 3;
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getAscent() + metrics.getDescent();
        int width = textWidth + horizontalPadding * 2;
        int height = textHeight + verticalPadding * 2;
        int x = centerX - width / 2;
        int y = centerY - height / 2;

        graphics.setColor(new Color(0xFF, 0xFF, 0xFF, 0xF2));
        graphics.fillRoundRect(x, y, width, height, 8, 8);
        graphics.setStroke(new BasicStroke(1.0f));
        graphics.setColor(new Color(0xC8, 0xC8, 0xC8));
        graphics.drawRoundRect(x, y, width, height, 8, 8);

        graphics.setColor(TEXT_COLOR);
        drawCenteredText(graphics, text, centerX, centerY, metrics);
    }

    private static int pushOutsideRing(int centerX, int centerY, int labelCenterX, int labelCenterY, int textWidth, int outerRadius, int minCenterX, int maxCenterX, int horizontalDirection) {
        double dy = labelCenterY - centerY;
        double safeRadius = outerRadius + textWidth / 2.0 + 6.0;
        if (Math.hypot(labelCenterX - centerX, dy) >= safeRadius) {
            return labelCenterX;
        }

        double requiredDx = Math.abs(dy) >= safeRadius ? 0.0 : Math.sqrt(safeRadius * safeRadius - dy * dy);
        int shiftedX = (int) Math.round(centerX + horizontalDirection * requiredDx);
        return clamp(shiftedX, minCenterX, maxCenterX);
    }

    private static void drawLegend(Graphics2D graphics, int x, int centerY, Options options) {
        int boxWidth = 26;
        int boxHeight = 12;
        int gap = 8;
        int rowGap = 24;
        int fontSize = 14;
        int firstY = centerY - rowGap / 2 - boxHeight;

        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
        drawLegendRow(graphics, x, firstY, boxWidth, boxHeight, gap, PASSED_COLOR, options.passed + " passed");
        drawLegendRow(graphics, x, firstY + rowGap, boxWidth, boxHeight, gap, FAILED_COLOR, options.failed + " failed");
    }

    private static void drawEmptyLegend(Graphics2D graphics, int x, int centerY) {
        int boxWidth = 26;
        int boxHeight = 12;
        int gap = 8;
        int y = centerY - boxHeight / 2;

        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        drawLegendRow(graphics, x, y, boxWidth, boxHeight, gap, EMPTY_COLOR, "No results");
    }

    private static void drawLegendRow(Graphics2D graphics, int x, int y, int boxWidth, int boxHeight, int gap, Color color, String label) {
        graphics.setColor(color);
        graphics.fillRect(x, y, boxWidth, boxHeight);
        graphics.setColor(LEGEND_TEXT_COLOR);
        FontMetrics metrics = graphics.getFontMetrics();
        graphics.drawString(label, x + boxWidth + gap, y + boxHeight / 2 + (metrics.getAscent() - metrics.getDescent()) / 2);
    }

    private static void applyQualityHints(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }

    private static String trimToFit(Graphics2D graphics, String value, int maxWidth) {
        FontMetrics metrics = graphics.getFontMetrics();
        if (metrics.stringWidth(value) <= maxWidth) {
            return value;
        }

        String suffix = "...";
        int suffixWidth = metrics.stringWidth(suffix);
        StringBuilder result = new StringBuilder(value);
        while (!result.isEmpty() && metrics.stringWidth(result.toString()) + suffixWidth > maxWidth) {
            result.deleteCharAt(result.length() - 1);
        }
        return result + suffix;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> values = new HashMap<>();
        for (int index = 0; index < args.length; index++) {
            String arg = args[index];
            if (!arg.startsWith("--")) {
                throw new IllegalArgumentException("Unexpected argument: " + arg);
            }

            int equalsIndex = arg.indexOf('=');
            if (equalsIndex > 0) {
                values.put(arg.substring(2, equalsIndex), arg.substring(equalsIndex + 1));
                continue;
            }

            if (index + 1 >= args.length || args[index + 1].startsWith("--")) {
                throw new IllegalArgumentException("Missing value for " + arg);
            }
            values.put(arg.substring(2), args[++index]);
        }
        return values;
    }

    private static boolean hasFlag(String[] args, String flag) {
        for (String arg : args) {
            if (flag.equals(arg)) {
                return true;
            }
        }
        return false;
    }

    private static void printUsage() {
        System.out.println("""
                Usage:
                  java -Xms16m -Xmx64m -Djava.awt.headless=true -jar chart-renderer.jar \\
                    --passed 19 --failed 1 --output chart.png [--width 400 --height 300 --scale 2 --title "Test target cart"]

                Options:
                  --passed   Passed test count. Required.
                  --failed   Failed test count. Required.
                  --output   Output PNG path. Required.
                  --width    Logical chart width. Default: 400.
                  --height   Logical chart height. Default: 300.
                  --scale    Pixel scale factor for crisp email rendering. Default: 2.
                  --title    Optional title rendered above the chart.
                """);
    }

    private static final class Options {
        private final int passed;
        private final int failed;
        private final int width;
        private final int height;
        private final float scale;
        private final float cutout;
        private final String title;
        private final Path output;

        private Options(int passed, int failed, int width, int height, float scale, float cutout, String title, Path output) {
            this.passed = passed;
            this.failed = failed;
            this.width = width;
            this.height = height;
            this.scale = scale;
            this.cutout = cutout;
            this.title = title;
            this.output = output;
        }

        private static Options from(Map<String, String> args) {
            int passed = parseNonNegativeInt(args, "passed", true, 0);
            int failed = parseNonNegativeInt(args, "failed", true, 0);
            int width = parseNonNegativeInt(args, "width", false, 400);
            int height = parseNonNegativeInt(args, "height", false, 300);
            float scale = parseFloat(args, "scale", 2.0f);
            float cutout = parseFloat(args, "cutout", 0.63f);
            String outputValue = args.get("output");

            if (outputValue == null || outputValue.isBlank()) {
                throw new IllegalArgumentException("--output is required");
            }
            if (width < 240 || height < 180) {
                throw new IllegalArgumentException("width must be >= 240 and height must be >= 180");
            }
            if (scale < 1.0f || scale > 4.0f) {
                throw new IllegalArgumentException("scale must be between 1 and 4");
            }
            if (cutout < 0.35f || cutout > 0.80f) {
                throw new IllegalArgumentException("cutout must be between 0.35 and 0.80");
            }

            return new Options(
                    passed,
                    failed,
                    width,
                    height,
                    scale,
                    cutout,
                    args.getOrDefault("title", ""),
                    Path.of(outputValue)
            );
        }

        private int total() {
            return passed + failed;
        }

        private static int parseNonNegativeInt(Map<String, String> args, String key, boolean required, int defaultValue) {
            String value = args.get(key);
            if ((value == null || value.isBlank()) && required) {
                throw new IllegalArgumentException("--" + key + " is required");
            }
            if (value == null || value.isBlank()) {
                return defaultValue;
            }

            try {
                int parsed = Integer.parseInt(value);
                if (parsed < 0) {
                    throw new IllegalArgumentException("--" + key + " must be >= 0");
                }
                return parsed;
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("--" + key + " must be an integer", ex);
            }
        }

        private static float parseFloat(Map<String, String> args, String key, float defaultValue) {
            String value = args.get(key);
            if (value == null || value.isBlank()) {
                return defaultValue;
            }

            try {
                return Float.parseFloat(value);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("--" + key + " must be a number", ex);
            }
        }
    }
}

package io.github.ciscoadmin.tachart.render;

import io.github.ciscoadmin.tachart.model.RenderOptions;
import io.github.ciscoadmin.tachart.model.TestSummary;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;

public final class Java2DChartRenderer {
    private final ChartTheme theme;
    private final ChartLayoutCalculator layoutCalculator;
    private final PercentLabelRenderer percentLabelRenderer;
    private final LegendRenderer legendRenderer;

    public Java2DChartRenderer() {
        this.theme = ChartTheme.defaultTheme();
        this.layoutCalculator = new ChartLayoutCalculator();
        this.percentLabelRenderer = new PercentLabelRenderer(theme, new PercentLabelPolicy());
        this.legendRenderer = new LegendRenderer(theme);
    }

    public BufferedImage render(TestSummary summary, RenderOptions options) {
        BufferedImage image = new BufferedImage(
                options.pixelWidth(),
                options.pixelHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = image.createGraphics();
        try {
            graphics.scale(options.scale(), options.scale());
            applyQualityHints(graphics);
            drawChart(graphics, summary, options);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    private void drawChart(Graphics2D graphics, TestSummary summary, RenderOptions options) {
        graphics.setColor(theme.backgroundColor());
        graphics.fillRect(0, 0, options.width(), options.height());

        int titleHeight = drawTitle(graphics, options);
        int legendWidth = summary.hasResults()
                ? legendRenderer.requiredWidth(graphics, summary)
                : legendRenderer.requiredEmptyWidth(graphics);
        ChartLayout layout = layoutCalculator.calculate(options, titleHeight, legendWidth);

        if (!summary.hasResults()) {
            drawEmptyChart(graphics, layout, options.width());
            return;
        }

        drawDoughnut(graphics, layout, summary);
        drawCenterTotal(graphics, layout.centerX(), layout.centerY(), layout.outerRadius(), summary.total());
        percentLabelRenderer.draw(graphics, layout, summary, options);
        legendRenderer.draw(graphics, layout.legendX(), layout.centerY(), options.width(), summary);
    }

    private int drawTitle(Graphics2D graphics, RenderOptions options) {
        if (options.title().isBlank()) {
            return 0;
        }

        int fontSize = Math.max(13, Math.round(options.height() * 0.055f));
        graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
        graphics.setColor(theme.secondaryTextColor());

        FontMetrics metrics = graphics.getFontMetrics();
        String title = TextDrawing.trimToFit(metrics, options.title(), options.width() - 24);
        int x = (options.width() - metrics.stringWidth(title)) / 2;
        int baseline = 10 + metrics.getAscent();
        graphics.drawString(title, x, baseline);
        return baseline + metrics.getDescent() + 4;
    }

    private void drawDoughnut(Graphics2D graphics, ChartLayout layout, TestSummary summary) {
        double failedAngle = summary.failedAngleDegrees();
        double passedAngle = 360.0 - failedAngle;

        if (summary.failed() == 0 || failedAngle <= 0.0) {
            fillAndOutline(graphics, ring(layout), theme.passedColor());
            return;
        }
        if (summary.passed() == 0 || passedAngle <= 0.0) {
            fillAndOutline(graphics, ring(layout), theme.failedColor());
            return;
        }

        Shape passedSegment = RingGeometry.segment(
                layout.centerX(),
                layout.centerY(),
                layout.outerRadius(),
                layout.innerRadius(),
                90.0,
                passedAngle
        );
        Shape failedSegment = RingGeometry.segment(
                layout.centerX(),
                layout.centerY(),
                layout.outerRadius(),
                layout.innerRadius(),
                90.0 - passedAngle,
                failedAngle
        );

        fillAndOutline(graphics, passedSegment, theme.passedColor());
        fillAndOutline(graphics, failedSegment, theme.failedColor());
    }

    private void drawEmptyChart(Graphics2D graphics, ChartLayout layout, int canvasWidth) {
        fillAndOutline(graphics, ring(layout), theme.emptyColor());
        drawCenterTotal(
                graphics,
                layout.centerX(),
                layout.centerY() - Math.round(layout.outerRadius() * 0.06f),
                layout.outerRadius(),
                0
        );

        int fontSize = Math.max(13, Math.round(layout.outerRadius() * 0.075f));
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
        graphics.setColor(theme.secondaryTextColor());

        FontMetrics metrics = graphics.getFontMetrics();
        String label = "No test data";
        int labelX = layout.centerX() - metrics.stringWidth(label) / 2;
        int labelBaseline = layout.centerY() + Math.round(layout.outerRadius() * 0.22f);
        graphics.drawString(label, labelX, labelBaseline);
        legendRenderer.drawEmpty(graphics, layout.legendX(), layout.centerY(), canvasWidth);
    }

    private void fillAndOutline(Graphics2D graphics, Shape shape, Color color) {
        graphics.setColor(color);
        graphics.fill(shape);
        graphics.setStroke(new BasicStroke(2.0f));
        graphics.setColor(theme.backgroundColor());
        graphics.draw(shape);
    }

    private static Shape ring(ChartLayout layout) {
        return RingGeometry.ring(
                layout.centerX(),
                layout.centerY(),
                layout.outerRadius(),
                layout.innerRadius()
        );
    }

    private static void drawCenterTotal(
            Graphics2D graphics,
            int centerX,
            int centerY,
            int outerRadius,
            long total
    ) {
        int fontSize = Math.max(32, Math.round(outerRadius * 0.36f));
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
        graphics.setColor(Color.BLACK);
        TextDrawing.drawCentered(
                graphics,
                Long.toString(total),
                centerX,
                centerY,
                graphics.getFontMetrics()
        );
    }

    private static void applyQualityHints(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
    }
}

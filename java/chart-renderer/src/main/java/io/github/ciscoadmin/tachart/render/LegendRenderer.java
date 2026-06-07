package io.github.ciscoadmin.tachart.render;

import io.github.ciscoadmin.tachart.model.TestSummary;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

final class LegendRenderer {
    private static final int CANVAS_MARGIN = 4;
    private static final int BOX_WIDTH = 26;
    private static final int BOX_HEIGHT = 12;
    private static final int LABEL_GAP = 8;
    private static final int FONT_SIZE = 14;

    private final ChartTheme theme;

    LegendRenderer(ChartTheme theme) {
        this.theme = theme;
    }

    int requiredWidth(Graphics2D graphics, TestSummary summary, LegendFormat format) {
        FontMetrics metrics = configureFont(graphics);
        int labelWidth = Math.max(
                metrics.stringWidth(passedLabel(summary, format)),
                metrics.stringWidth(failedLabel(summary, format))
        );
        return BOX_WIDTH + LABEL_GAP + labelWidth;
    }

    int requiredEmptyWidth(Graphics2D graphics) {
        FontMetrics metrics = configureFont(graphics);
        return BOX_WIDTH + LABEL_GAP + metrics.stringWidth("No results");
    }

    void draw(
            Graphics2D graphics,
            int x,
            int centerY,
            int canvasWidth,
            TestSummary summary,
            LegendFormat format
    ) {
        int rowGap = 24;
        int firstY = centerY - rowGap / 2 - BOX_HEIGHT;

        configureFont(graphics);
        drawRow(
                graphics,
                x,
                firstY,
                canvasWidth,
                theme.passedColor(),
                passedLabel(summary, format)
        );
        drawRow(
                graphics,
                x,
                firstY + rowGap,
                canvasWidth,
                theme.failedColor(),
                failedLabel(summary, format)
        );
    }

    void drawEmpty(Graphics2D graphics, int x, int centerY, int canvasWidth) {
        int y = centerY - BOX_HEIGHT / 2;
        configureFont(graphics);
        drawRow(graphics, x, y, canvasWidth, theme.emptyColor(), "No results");
    }

    private void drawRow(
            Graphics2D graphics,
            int x,
            int y,
            int canvasWidth,
            Color color,
            String label
    ) {
        graphics.setColor(color);
        graphics.fillRect(x, y, BOX_WIDTH, BOX_HEIGHT);
        graphics.setColor(theme.secondaryTextColor());

        FontMetrics metrics = graphics.getFontMetrics();
        int labelX = x + BOX_WIDTH + LABEL_GAP;
        int availableLabelWidth = Math.max(0, canvasWidth - CANVAS_MARGIN - labelX);
        String visibleLabel = TextDrawing.trimToFit(metrics, label, availableLabelWidth);
        int baseline = y + BOX_HEIGHT / 2 + (metrics.getAscent() - metrics.getDescent()) / 2;
        graphics.drawString(visibleLabel, labelX, baseline);
    }

    private static FontMetrics configureFont(Graphics2D graphics) {
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, FONT_SIZE));
        return graphics.getFontMetrics();
    }

    private static String passedLabel(TestSummary summary, LegendFormat format) {
        return format == LegendFormat.COMPACT
                ? Long.toString(summary.passed())
                : summary.passed() + " passed";
    }

    private static String failedLabel(TestSummary summary, LegendFormat format) {
        return format == LegendFormat.COMPACT
                ? Long.toString(summary.failed())
                : summary.failed() + " failed";
    }
}

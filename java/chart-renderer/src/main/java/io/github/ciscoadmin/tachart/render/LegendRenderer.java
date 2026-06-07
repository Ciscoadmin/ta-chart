package io.github.ciscoadmin.tachart.render;

import io.github.ciscoadmin.tachart.model.TestSummary;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

final class LegendRenderer {
    private static final int BOX_WIDTH = 26;
    private static final int BOX_HEIGHT = 12;
    private static final int LABEL_GAP = 8;

    private final ChartTheme theme;

    LegendRenderer(ChartTheme theme) {
        this.theme = theme;
    }

    void draw(Graphics2D graphics, int x, int centerY, TestSummary summary) {
        int rowGap = 24;
        int firstY = centerY - rowGap / 2 - BOX_HEIGHT;

        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        drawRow(graphics, x, firstY, theme.passedColor(), summary.passed() + " passed");
        drawRow(graphics, x, firstY + rowGap, theme.failedColor(), summary.failed() + " failed");
    }

    void drawEmpty(Graphics2D graphics, int x, int centerY) {
        int y = centerY - BOX_HEIGHT / 2;
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
        drawRow(graphics, x, y, theme.emptyColor(), "No results");
    }

    private void drawRow(Graphics2D graphics, int x, int y, Color color, String label) {
        graphics.setColor(color);
        graphics.fillRect(x, y, BOX_WIDTH, BOX_HEIGHT);
        graphics.setColor(theme.secondaryTextColor());

        FontMetrics metrics = graphics.getFontMetrics();
        int baseline = y + BOX_HEIGHT / 2 + (metrics.getAscent() - metrics.getDescent()) / 2;
        graphics.drawString(label, x + BOX_WIDTH + LABEL_GAP, baseline);
    }
}

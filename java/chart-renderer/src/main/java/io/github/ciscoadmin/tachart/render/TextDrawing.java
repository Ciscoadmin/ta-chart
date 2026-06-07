package io.github.ciscoadmin.tachart.render;

import java.awt.FontMetrics;
import java.awt.Graphics2D;

final class TextDrawing {
    private TextDrawing() {
    }

    static void drawCentered(
            Graphics2D graphics,
            String text,
            int centerX,
            int centerY,
            FontMetrics metrics
    ) {
        int x = centerX - metrics.stringWidth(text) / 2;
        int y = centerY + (metrics.getAscent() - metrics.getDescent()) / 2;
        graphics.drawString(text, x, y);
    }

    static String trimToFit(FontMetrics metrics, String value, int maxWidth) {
        if (metrics.stringWidth(value) <= maxWidth) {
            return value;
        }

        String suffix = "...";
        if (metrics.stringWidth(suffix) > maxWidth) {
            return "";
        }

        int lowerBound = 0;
        int upperBound = value.length();
        while (lowerBound < upperBound) {
            int candidateLength = (lowerBound + upperBound + 1) / 2;
            String candidate = value.substring(0, candidateLength) + suffix;
            if (metrics.stringWidth(candidate) <= maxWidth) {
                lowerBound = candidateLength;
            } else {
                upperBound = candidateLength - 1;
            }
        }
        return value.substring(0, lowerBound) + suffix;
    }
}

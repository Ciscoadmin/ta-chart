package io.github.ciscoadmin.tachart.render;

import org.junit.jupiter.api.Test;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextDrawingTest {
    @Test
    void preservesTextThatAlreadyFits() {
        FontMetrics metrics = fontMetrics();

        assertEquals("Smoke", TextDrawing.trimToFit(metrics, "Smoke", 200));
    }

    @Test
    void truncatesLongTextWithinRequestedWidth() {
        FontMetrics metrics = fontMetrics();

        String result = TextDrawing.trimToFit(
                metrics,
                "A very long regression feature title that does not fit",
                120
        );

        assertTrue(result.endsWith("..."));
        assertTrue(metrics.stringWidth(result) <= 120);
    }

    private static FontMetrics fontMetrics() {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
            return graphics.getFontMetrics();
        } finally {
            graphics.dispose();
        }
    }
}

package io.github.ciscoadmin.tachart.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RenderOptionsTest {
    @Test
    void calculatesScaledPixelDimensions() {
        RenderOptions options = new RenderOptions(400, 300, 2.0, 0.63, null);

        assertEquals(800, options.pixelWidth());
        assertEquals(600, options.pixelHeight());
        assertEquals("", options.title());
    }

    @Test
    void rejectsNonFiniteValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RenderOptions(400, 300, Double.NaN, 0.63, "")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new RenderOptions(400, 300, 2.0, Double.POSITIVE_INFINITY, "")
        );
    }

    @Test
    void rejectsImagesThatExceedMemoryGuard() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new RenderOptions(4096, 4096, 1.0, 0.63, "")
        );
    }
}

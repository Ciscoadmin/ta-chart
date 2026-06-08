package io.github.ciscoadmin.tachart.render;

import io.github.ciscoadmin.tachart.model.RenderOptions;
import io.github.ciscoadmin.tachart.model.TestSummary;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Java2DChartRendererTest {
    private static final Color PASSED_COLOR = new Color(0x8A, 0xD6, 0x8B);
    private static final Color FAILED_COLOR = new Color(0xFF, 0x5A, 0x5A);
    private static final Color EMPTY_COLOR = new Color(0xDD, 0xDD, 0xDD);
    private static final Color BADGE_BORDER_COLOR = new Color(0xC8, 0xC8, 0xC8);

    private final Java2DChartRenderer renderer = new Java2DChartRenderer();

    @ParameterizedTest
    @MethodSource("renderCases")
    void rendersRepresentativeCases(
            long passed,
            long failed,
            String title,
            Color expectedColor
    ) {
        BufferedImage image = renderer.render(
                new TestSummary(passed, failed),
                new RenderOptions(400, 300, 2.0, 0.63, title)
        );

        assertEquals(800, image.getWidth());
        assertEquals(600, image.getHeight());
        assertTrue(countExactColor(image, expectedColor) > 1_000);
        assertTrue(countNonWhitePixels(image) > 10_000);
    }

    @ParameterizedTest
    @MethodSource("boundedLayoutCases")
    void keepsBadgeAndLegendInsideCanvas(
            int width,
            int height,
            long passed,
            long failed,
            String title
    ) {
        double scale = 2.0;
        BufferedImage image = renderer.render(
                new TestSummary(passed, failed),
                new RenderOptions(width, height, scale, 0.63, title)
        );

        int margin = (int) scale * 2;
        assertTrue(isColorAbsentFromTopMargin(image, BADGE_BORDER_COLOR, margin));
        assertTrue(isColorAbsentFromLeftMargin(image, Color.BLACK, margin));
        assertTrue(isWhiteRightMargin(image, margin));
    }

    private static Stream<Arguments> renderCases() {
        return Stream.of(
                Arguments.of(598, 0, "", PASSED_COLOR),
                Arguments.of(0, 767, "", FAILED_COLOR),
                Arguments.of(0, 0, "", EMPTY_COLOR),
                Arguments.of(31, 1, "Regress feature", PASSED_COLOR),
                Arguments.of(19, 1, "", FAILED_COLOR),
                Arguments.of(936, 7672, "", FAILED_COLOR),
                Arguments.of(1, 25, "Smoke", FAILED_COLOR)
        );
    }

    private static Stream<Arguments> boundedLayoutCases() {
        return Stream.of(
                Arguments.of(400, 300, 31, 1, ""),
                Arguments.of(240, 180, 31, 1, ""),
                Arguments.of(240, 180, 99_999, 1, "")
        );
    }

    private static long countExactColor(BufferedImage image, Color color) {
        int expected = color.getRGB();
        long count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getRGB(x, y) == expected) {
                    count++;
                }
            }
        }
        return count;
    }

    private static long countNonWhitePixels(BufferedImage image) {
        int white = Color.WHITE.getRGB();
        long count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getRGB(x, y) != white) {
                    count++;
                }
            }
        }
        return count;
    }

    private static boolean isColorAbsentFromTopMargin(
            BufferedImage image,
            Color color,
            int margin
    ) {
        int excludedColor = color.getRGB();
        for (int y = 0; y < margin; y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if (image.getRGB(x, y) == excludedColor) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isWhiteRightMargin(BufferedImage image, int margin) {
        int white = Color.WHITE.getRGB();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = image.getWidth() - margin; x < image.getWidth(); x++) {
                if (image.getRGB(x, y) != white) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean isColorAbsentFromLeftMargin(
            BufferedImage image,
            Color color,
            int margin
    ) {
        int excludedColor = color.getRGB();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < margin; x++) {
                if (image.getRGB(x, y) == excludedColor) {
                    return false;
                }
            }
        }
        return true;
    }
}

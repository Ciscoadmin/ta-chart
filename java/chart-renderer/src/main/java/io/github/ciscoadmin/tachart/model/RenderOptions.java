package io.github.ciscoadmin.tachart.model;

public record RenderOptions(int width, int height, double scale, double cutout, String title) {
    public static final int DEFAULT_WIDTH = 400;
    public static final int DEFAULT_HEIGHT = 300;
    public static final double DEFAULT_SCALE = 2.0;
    public static final double DEFAULT_CUTOUT = 0.63;

    private static final int MIN_WIDTH = 240;
    private static final int MIN_HEIGHT = 180;
    private static final int MAX_LOGICAL_DIMENSION = 4096;
    private static final long MAX_PIXEL_COUNT = 4_000_000L;

    public RenderOptions {
        if (width < MIN_WIDTH || height < MIN_HEIGHT) {
            throw new IllegalArgumentException("width must be >= 240 and height must be >= 180");
        }
        if (width > MAX_LOGICAL_DIMENSION || height > MAX_LOGICAL_DIMENSION) {
            throw new IllegalArgumentException("width and height must be <= " + MAX_LOGICAL_DIMENSION);
        }
        if (!Double.isFinite(scale) || scale < 1.0 || scale > 4.0) {
            throw new IllegalArgumentException("scale must be a finite number between 1 and 4");
        }
        if (!Double.isFinite(cutout) || cutout < 0.35 || cutout > 0.80) {
            throw new IllegalArgumentException("cutout must be a finite number between 0.35 and 0.80");
        }

        title = title == null ? "" : title;

        long pixelCount = Math.round(width * scale) * Math.round(height * scale);
        if (pixelCount > MAX_PIXEL_COUNT) {
            throw new IllegalArgumentException(
                    "rendered image is too large: " + pixelCount + " pixels; maximum is " + MAX_PIXEL_COUNT
            );
        }
    }

    public int pixelWidth() {
        return (int) Math.round(width * scale);
    }

    public int pixelHeight() {
        return (int) Math.round(height * scale);
    }
}

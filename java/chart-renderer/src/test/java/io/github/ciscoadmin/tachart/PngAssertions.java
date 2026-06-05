package io.github.ciscoadmin.tachart;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public final class PngAssertions {
    private PngAssertions() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            throw new IllegalArgumentException("Usage: PngAssertions <path> <expectedWidth> <expectedHeight> <minBytes>");
        }

        File file = new File(args[0]);
        int expectedWidth = Integer.parseInt(args[1]);
        int expectedHeight = Integer.parseInt(args[2]);
        long minBytes = Long.parseLong(args[3]);

        if (!file.isFile()) {
            throw new AssertionError("PNG file does not exist: " + file);
        }
        if (file.length() < minBytes) {
            throw new AssertionError("PNG file is too small: " + file.length() + " bytes");
        }

        BufferedImage image = ImageIO.read(file);
        if (image == null) {
            throw new AssertionError("File is not a readable PNG: " + file);
        }
        if (image.getWidth() != expectedWidth || image.getHeight() != expectedHeight) {
            throw new AssertionError("Unexpected image size: " + image.getWidth() + "x" + image.getHeight());
        }
    }
}

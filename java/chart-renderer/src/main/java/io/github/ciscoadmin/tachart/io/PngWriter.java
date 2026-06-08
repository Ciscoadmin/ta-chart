package io.github.ciscoadmin.tachart.io;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class PngWriter {
    public void write(BufferedImage image, Path output) throws IOException {
        Path absoluteOutput = output.toAbsolutePath();
        if (Files.isDirectory(absoluteOutput)) {
            throw new IOException("output path is a directory: " + absoluteOutput);
        }

        Path parent = absoluteOutput.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        if (!ImageIO.write(image, "png", absoluteOutput.toFile())) {
            throw new IOException("no PNG image writer is available");
        }
    }
}

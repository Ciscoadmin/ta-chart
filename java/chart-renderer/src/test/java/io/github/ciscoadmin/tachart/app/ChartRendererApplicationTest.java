package io.github.ciscoadmin.tachart.app;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChartRendererApplicationTest {
    private final ChartRendererApplication application = new ChartRendererApplication();

    @TempDir
    Path temporaryDirectory;

    @Test
    void writesPngAndReturnsSuccess() throws IOException {
        Path output = temporaryDirectory.resolve("nested/chart.png");
        CapturedOutput captured = run(
                "--passed", "31",
                "--failed", "1",
                "--title", "Regress feature",
                "--output", output.toString()
        );

        assertEquals(0, captured.exitCode());
        assertTrue(Files.isRegularFile(output));
        BufferedImage image = ImageIO.read(output.toFile());
        assertNotNull(image);
        assertEquals(800, image.getWidth());
        assertTrue(captured.standardOutput().contains("Chart written to"));
    }

    @Test
    void returnsUsageErrorForInvalidArguments() {
        CapturedOutput captured = run(
                "--passed", "-1",
                "--failed", "0",
                "--output", temporaryDirectory.resolve("chart.png").toString()
        );

        assertEquals(2, captured.exitCode());
        assertTrue(captured.errorOutput().contains("--passed must be >= 0"));
    }

    @Test
    void returnsWriteErrorWhenOutputPointsToDirectory() throws IOException {
        Path outputDirectory = Files.createDirectory(temporaryDirectory.resolve("chart.png"));
        CapturedOutput captured = run(
                "--passed", "1",
                "--failed", "0",
                "--output", outputDirectory.toString()
        );

        assertEquals(1, captured.exitCode());
        assertTrue(captured.errorOutput().contains("unable to write chart image"));
    }

    @Test
    void printsHelpWithoutRendering() {
        CapturedOutput captured = run("--help");

        assertEquals(0, captured.exitCode());
        assertTrue(captured.standardOutput().contains("Usage:"));
    }

    private CapturedOutput run(String... args) {
        ByteArrayOutputStream standardOutput = new ByteArrayOutputStream();
        ByteArrayOutputStream errorOutput = new ByteArrayOutputStream();

        int exitCode = application.run(
                args,
                new PrintStream(standardOutput, true, StandardCharsets.UTF_8),
                new PrintStream(errorOutput, true, StandardCharsets.UTF_8)
        );
        return new CapturedOutput(
                exitCode,
                standardOutput.toString(StandardCharsets.UTF_8),
                errorOutput.toString(StandardCharsets.UTF_8)
        );
    }

    private record CapturedOutput(int exitCode, String standardOutput, String errorOutput) {
    }
}

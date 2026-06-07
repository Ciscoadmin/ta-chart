package io.github.ciscoadmin.tachart.app;

import io.github.ciscoadmin.tachart.cli.ChartCommand;
import io.github.ciscoadmin.tachart.cli.CliParser;
import io.github.ciscoadmin.tachart.cli.CliUsageException;
import io.github.ciscoadmin.tachart.io.PngWriter;
import io.github.ciscoadmin.tachart.render.Java2DChartRenderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PrintStream;

public final class ChartRendererApplication {
    private final CliParser cliParser;
    private final Java2DChartRenderer renderer;
    private final PngWriter pngWriter;

    public ChartRendererApplication() {
        this(new CliParser(), new Java2DChartRenderer(), new PngWriter());
    }

    ChartRendererApplication(CliParser cliParser, Java2DChartRenderer renderer, PngWriter pngWriter) {
        this.cliParser = cliParser;
        this.renderer = renderer;
        this.pngWriter = pngWriter;
    }

    public int run(String[] args, PrintStream standardOutput, PrintStream errorOutput) {
        if (cliParser.isHelpRequested(args)) {
            standardOutput.print(CliParser.usage());
            return 0;
        }

        try {
            ChartCommand command = cliParser.parse(args);
            BufferedImage image = renderer.render(command.summary(), command.options());
            pngWriter.write(image, command.output());
            standardOutput.println("Chart written to " + command.output().toAbsolutePath());
            return 0;
        } catch (CliUsageException exception) {
            errorOutput.println("Error: " + exception.getMessage());
            errorOutput.println("Use --help to show usage.");
            return 2;
        } catch (IOException exception) {
            errorOutput.println("Error: unable to write chart image: " + exception.getMessage());
            return 1;
        }
    }
}

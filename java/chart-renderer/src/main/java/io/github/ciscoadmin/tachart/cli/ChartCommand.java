package io.github.ciscoadmin.tachart.cli;

import io.github.ciscoadmin.tachart.model.RenderOptions;
import io.github.ciscoadmin.tachart.model.TestSummary;

import java.nio.file.Path;
import java.util.Objects;

public record ChartCommand(TestSummary summary, RenderOptions options, Path output) {
    public ChartCommand {
        Objects.requireNonNull(summary, "summary");
        Objects.requireNonNull(options, "options");
        Objects.requireNonNull(output, "output");
    }
}

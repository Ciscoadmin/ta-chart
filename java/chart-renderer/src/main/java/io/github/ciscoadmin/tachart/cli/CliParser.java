package io.github.ciscoadmin.tachart.cli;

import io.github.ciscoadmin.tachart.model.RenderOptions;
import io.github.ciscoadmin.tachart.model.TestSummary;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public final class CliParser {
    private static final Set<String> SUPPORTED_OPTIONS = Set.of(
            "passed",
            "failed",
            "output",
            "width",
            "height",
            "scale",
            "cutout",
            "title"
    );

    public boolean isHelpRequested(String[] args) {
        if (args.length == 0) {
            return true;
        }

        for (String argument : args) {
            if ("--help".equals(argument) || "-h".equals(argument)) {
                return true;
            }
        }
        return false;
    }

    public ChartCommand parse(String[] args) {
        Map<String, String> values = parseValues(args);

        try {
            TestSummary summary = new TestSummary(
                    parseRequiredLong(values, "passed"),
                    parseRequiredLong(values, "failed")
            );
            RenderOptions options = new RenderOptions(
                    parseInt(values, "width", RenderOptions.DEFAULT_WIDTH),
                    parseInt(values, "height", RenderOptions.DEFAULT_HEIGHT),
                    parseDouble(values, "scale", RenderOptions.DEFAULT_SCALE),
                    parseDouble(values, "cutout", RenderOptions.DEFAULT_CUTOUT),
                    values.getOrDefault("title", "")
            );
            return new ChartCommand(summary, options, parseOutput(values));
        } catch (IllegalArgumentException exception) {
            if (exception instanceof CliUsageException usageException) {
                throw usageException;
            }
            throw new CliUsageException(exception.getMessage(), exception);
        }
    }

    public static String usage() {
        return """
                Usage:
                  java -Xms16m -Xmx64m -Djava.awt.headless=true -jar chart-renderer.jar \\
                    --passed 19 --failed 1 --output chart.png [--width 400 --height 300 --scale 2 --title "Test target chart"]

                Options:
                  --passed   Passed test count. Required.
                  --failed   Failed test count. Required.
                  --output   Output PNG path. Required.
                  --width    Logical chart width. Default: 400.
                  --height   Logical chart height. Default: 300.
                  --scale    Pixel scale factor. Range: 1-4. Default: 2.
                  --cutout   Doughnut cutout ratio. Range: 0.35-0.80. Default: 0.63.
                  --title    Optional title rendered above the chart.
                  --help     Show this help.
                """;
    }

    private static Map<String, String> parseValues(String[] args) {
        Map<String, String> values = new LinkedHashMap<>();

        for (int index = 0; index < args.length; index++) {
            String argument = args[index];
            if (!argument.startsWith("--")) {
                throw new CliUsageException("Unexpected argument: " + argument);
            }

            int equalsIndex = argument.indexOf('=');
            String key;
            String value;
            if (equalsIndex >= 0) {
                key = argument.substring(2, equalsIndex);
                value = argument.substring(equalsIndex + 1);
            } else {
                key = argument.substring(2);
                if (index + 1 >= args.length || args[index + 1].startsWith("--")) {
                    throw new CliUsageException("Missing value for " + argument);
                }
                value = args[++index];
            }

            if (!SUPPORTED_OPTIONS.contains(key)) {
                throw new CliUsageException("Unknown option: --" + key);
            }
            if (values.putIfAbsent(key, value) != null) {
                throw new CliUsageException("Duplicate option: --" + key);
            }
        }

        return values;
    }

    private static long parseRequiredLong(Map<String, String> values, String key) {
        String value = requiredValue(values, key);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new CliUsageException("--" + key + " must be an integer", exception);
        }
    }

    private static int parseInt(Map<String, String> values, String key, int defaultValue) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new CliUsageException("--" + key + " must be an integer", exception);
        }
    }

    private static double parseDouble(Map<String, String> values, String key, double defaultValue) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        try {
            double parsed = Double.parseDouble(value);
            if (!Double.isFinite(parsed)) {
                throw new CliUsageException("--" + key + " must be a finite number");
            }
            return parsed;
        } catch (NumberFormatException exception) {
            throw new CliUsageException("--" + key + " must be a number", exception);
        }
    }

    private static Path parseOutput(Map<String, String> values) {
        String value = requiredValue(values, "output");
        try {
            return Path.of(value);
        } catch (InvalidPathException exception) {
            throw new CliUsageException("--output is not a valid path", exception);
        }
    }

    private static String requiredValue(Map<String, String> values, String key) {
        String value = values.get(key);
        if (value == null || value.isBlank()) {
            throw new CliUsageException("--" + key + " is required");
        }
        return value;
    }
}

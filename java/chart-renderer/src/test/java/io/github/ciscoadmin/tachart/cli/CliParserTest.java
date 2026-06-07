package io.github.ciscoadmin.tachart.cli;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CliParserTest {
    private final CliParser parser = new CliParser();

    @Test
    void parsesEqualsSyntaxAndDefaults() {
        ChartCommand command = parser.parse(new String[]{
                "--passed=31",
                "--failed=1",
                "--output=chart.png",
                "--title=Regress feature"
        });

        assertEquals(31, command.summary().passed());
        assertEquals(1, command.summary().failed());
        assertEquals(400, command.options().width());
        assertEquals(2.0, command.options().scale());
        assertEquals("Regress feature", command.options().title());
    }

    @ParameterizedTest
    @ValueSource(strings = {"NaN", "Infinity", "-Infinity"})
    void rejectsNonFiniteScale(String value) {
        CliUsageException exception = assertThrows(
                CliUsageException.class,
                () -> parser.parse(requiredArgs("--scale", value))
        );

        assertTrue(exception.getMessage().contains("finite"));
    }

    @Test
    void rejectsUnknownAndDuplicateOptions() {
        assertThrows(
                CliUsageException.class,
                () -> parser.parse(requiredArgs("--colour", "red"))
        );
        assertThrows(
                CliUsageException.class,
                () -> parser.parse(new String[]{
                        "--passed", "1",
                        "--passed", "2",
                        "--failed", "0",
                        "--output", "chart.png"
                })
        );
    }

    @Test
    void treatsNoArgumentsAndHelpFlagAsHelpRequests() {
        assertTrue(parser.isHelpRequested(new String[0]));
        assertTrue(parser.isHelpRequested(new String[]{"--help"}));
        assertTrue(parser.isHelpRequested(new String[]{"-h"}));
    }

    private static String[] requiredArgs(String extraKey, String extraValue) {
        return new String[]{
                "--passed", "1",
                "--failed", "0",
                "--output", "chart.png",
                extraKey, extraValue
        };
    }
}

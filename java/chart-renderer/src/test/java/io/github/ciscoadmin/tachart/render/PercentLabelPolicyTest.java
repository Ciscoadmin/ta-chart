package io.github.ciscoadmin.tachart.render;

import io.github.ciscoadmin.tachart.model.TestSummary;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PercentLabelPolicyTest {
    private final PercentLabelPolicy policy = new PercentLabelPolicy();

    @ParameterizedTest(name = "{0} passed, {1} failed -> {2}")
    @MethodSource("agreedExamples")
    void choosesExpectedPlacement(long passed, long failed, PercentLabelPlacement expected) {
        PercentLabelPlacement actual = policy.choose(
                new TestSummary(passed, failed),
                112.0,
                51,
                27,
                18
        );

        assertEquals(expected, actual);
    }

    private static Stream<Arguments> agreedExamples() {
        return Stream.of(
                Arguments.of(598, 0, PercentLabelPlacement.HIDDEN),
                Arguments.of(0, 767, PercentLabelPlacement.HIDDEN),
                Arguments.of(0, 0, PercentLabelPlacement.HIDDEN),
                Arguments.of(31, 1, PercentLabelPlacement.OUTSIDE),
                Arguments.of(19, 1, PercentLabelPlacement.INSIDE),
                Arguments.of(936, 7672, PercentLabelPlacement.INSIDE),
                Arguments.of(1, 25, PercentLabelPlacement.INSIDE)
        );
    }
}

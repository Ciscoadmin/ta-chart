package io.github.ciscoadmin.tachart.render;

import io.github.ciscoadmin.tachart.model.TestSummary;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LegendLayoutPolicyTest {
    private final LegendLayoutPolicy policy = new LegendLayoutPolicy();

    @Test
    void keepsFullLabelsForShortCounters() {
        assertEquals(
                LegendFormat.FULL,
                policy.choose(new TestSummary(31, 1), layout(50), layout(75))
        );
    }

    @Test
    void usesCompactLabelsWhenLongCountersMeaningfullyIncreaseChartSize() {
        assertEquals(
                LegendFormat.COMPACT,
                policy.choose(new TestSummary(9_999, 1), layout(50), layout(75))
        );
    }

    @Test
    void keepsFullLabelsWhenCompactModeDoesNotMeaningfullyIncreaseChartSize() {
        assertEquals(
                LegendFormat.FULL,
                policy.choose(new TestSummary(9_999, 1), layout(135), layout(140))
        );
    }

    private static ChartLayout layout(int outerRadius) {
        return new ChartLayout(0, 0, 0, 0, outerRadius, outerRadius / 2);
    }
}

package io.github.ciscoadmin.tachart.render;

import io.github.ciscoadmin.tachart.model.TestSummary;

final class LegendLayoutPolicy {
    private static final long LONG_COUNTER_THRESHOLD = 1_000;
    private static final int MIN_RADIUS_GAIN = 8;

    LegendFormat choose(
            TestSummary summary,
            ChartLayout fullLayout,
            ChartLayout compactLayout
    ) {
        long longestCounter = Math.max(summary.passed(), summary.failed());
        int radiusGain = compactLayout.outerRadius() - fullLayout.outerRadius();
        return longestCounter >= LONG_COUNTER_THRESHOLD && radiusGain >= MIN_RADIUS_GAIN
                ? LegendFormat.COMPACT
                : LegendFormat.FULL;
    }
}

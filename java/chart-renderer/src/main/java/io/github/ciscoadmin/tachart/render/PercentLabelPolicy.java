package io.github.ciscoadmin.tachart.render;

import io.github.ciscoadmin.tachart.model.TestSummary;

final class PercentLabelPolicy {
    private static final double MIN_VISIBLE_ANGLE_DEGREES = 4.0;

    PercentLabelPlacement choose(
            TestSummary summary,
            double labelRadius,
            int ringThickness,
            int textWidth,
            int textHeight
    ) {
        if (summary.failed() == 0 || summary.passed() == 0 || !summary.hasResults()) {
            return PercentLabelPlacement.HIDDEN;
        }

        double failedAngle = summary.failedAngleDegrees();
        if (failedAngle < MIN_VISIBLE_ANGLE_DEGREES) {
            return PercentLabelPlacement.HIDDEN;
        }

        double availableArcLength = labelRadius * Math.toRadians(failedAngle);
        boolean fitsArc = textWidth + 2 <= availableArcLength;
        boolean fitsThickness = textHeight + 4 <= ringThickness;
        return fitsArc && fitsThickness
                ? PercentLabelPlacement.INSIDE
                : PercentLabelPlacement.OUTSIDE;
    }
}

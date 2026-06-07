package io.github.ciscoadmin.tachart.render;

import io.github.ciscoadmin.tachart.model.RenderOptions;

final class ChartLayoutCalculator {
    private static final int CANVAS_MARGIN = 4;
    private static final int CHART_LEGEND_GAP = 12;
    private static final int MIN_OUTER_RADIUS = 30;

    ChartLayout calculate(RenderOptions options, int titleHeight, int legendWidth) {
        int preferredLegendX = Math.round(options.width() * 0.73f);
        int rightAlignedLegendX = options.width() - CANVAS_MARGIN - legendWidth;
        int minLegendX = CHART_LEGEND_GAP + MIN_OUTER_RADIUS * 2 + 4;
        int legendX = clamp(
                Math.min(preferredLegendX, rightAlignedLegendX),
                minLegendX,
                options.width() - CANVAS_MARGIN
        );
        int availableChartWidth = legendX - 12;
        int chartTop = titleHeight == 0 ? 0 : titleHeight + 4;
        int availableChartHeight = options.height() - chartTop;
        int centerX = availableChartWidth / 2;
        int centerY = chartTop + availableChartHeight / 2;
        int outerRadius = Math.max(
                MIN_OUTER_RADIUS,
                Math.min(availableChartWidth, availableChartHeight) / 2 - 2
        );
        int innerRadius = (int) Math.round(outerRadius * options.cutout());

        return new ChartLayout(
                titleHeight,
                legendX,
                centerX,
                centerY,
                outerRadius,
                innerRadius
        );
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

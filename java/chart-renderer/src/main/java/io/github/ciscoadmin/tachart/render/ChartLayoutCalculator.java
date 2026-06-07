package io.github.ciscoadmin.tachart.render;

import io.github.ciscoadmin.tachart.model.RenderOptions;

final class ChartLayoutCalculator {
    ChartLayout calculate(RenderOptions options, int titleHeight) {
        int legendX = Math.round(options.width() * 0.73f);
        int availableChartWidth = legendX - 12;
        int chartTop = titleHeight == 0 ? 0 : titleHeight + 4;
        int availableChartHeight = options.height() - chartTop;
        int centerX = availableChartWidth / 2;
        int centerY = chartTop + availableChartHeight / 2;
        int outerRadius = Math.max(30, Math.min(availableChartWidth, availableChartHeight) / 2 - 2);
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
}

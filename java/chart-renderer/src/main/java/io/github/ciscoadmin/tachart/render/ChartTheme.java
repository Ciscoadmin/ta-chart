package io.github.ciscoadmin.tachart.render;

import java.awt.Color;

record ChartTheme(
        Color passedColor,
        Color failedColor,
        Color emptyColor,
        Color textColor,
        Color secondaryTextColor,
        Color backgroundColor,
        Color leaderColor,
        Color badgeBorderColor
) {
    static ChartTheme defaultTheme() {
        return new ChartTheme(
                new Color(0x8A, 0xD6, 0x8B),
                new Color(0xFF, 0x5A, 0x5A),
                new Color(0xDD, 0xDD, 0xDD),
                new Color(0x11, 0x11, 0x11),
                new Color(0x66, 0x66, 0x66),
                Color.WHITE,
                new Color(0x99, 0x99, 0x99),
                new Color(0xC8, 0xC8, 0xC8)
        );
    }
}

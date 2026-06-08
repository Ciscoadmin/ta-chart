package io.github.ciscoadmin.tachart.render;

import io.github.ciscoadmin.tachart.model.RenderOptions;
import io.github.ciscoadmin.tachart.model.TestSummary;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

final class PercentLabelRenderer {
    private static final int CANVAS_MARGIN = 4;
    private static final int BADGE_HORIZONTAL_PADDING = 7;
    private static final int BADGE_VERTICAL_PADDING = 3;
    private static final int BADGE_RING_GAP = 6;
    private static final int LEADER_BADGE_GAP = 3;

    private final ChartTheme theme;
    private final PercentLabelPolicy placementPolicy;

    PercentLabelRenderer(ChartTheme theme, PercentLabelPolicy placementPolicy) {
        this.theme = theme;
        this.placementPolicy = placementPolicy;
    }

    void draw(
            Graphics2D graphics,
            ChartLayout layout,
            TestSummary summary,
            RenderOptions options
    ) {
        double failedAngle = summary.failedAngleDegrees();
        double passedAngle = 360.0 - failedAngle;
        double middleAngle = 90.0 - passedAngle - failedAngle / 2.0;
        double radians = Math.toRadians(middleAngle);
        double labelRadius = layout.innerRadius()
                + (layout.outerRadius() - layout.innerRadius()) * 0.50;

        String text = summary.failedPercentage() + "%";
        int fontSize = Math.max(14, Math.round(layout.outerRadius() * 0.135f));
        graphics.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, fontSize));
        graphics.setColor(theme.textColor());

        FontMetrics metrics = graphics.getFontMetrics();
        PercentLabelPlacement placement = placementPolicy.choose(
                summary,
                labelRadius,
                layout.outerRadius() - layout.innerRadius(),
                metrics.stringWidth(text),
                metrics.getAscent() + metrics.getDescent()
        );

        if (placement == PercentLabelPlacement.HIDDEN) {
            return;
        }
        if (placement == PercentLabelPlacement.INSIDE) {
            int x = (int) Math.round(layout.centerX() + labelRadius * Math.cos(radians));
            int y = (int) Math.round(layout.centerY() - labelRadius * Math.sin(radians));
            TextDrawing.drawCentered(graphics, text, x, y, metrics);
            return;
        }

        drawOutside(graphics, text, metrics, layout, options, radians);
    }

    private void drawOutside(
            Graphics2D graphics,
            String text,
            FontMetrics metrics,
            ChartLayout layout,
            RenderOptions options,
            double radians
    ) {
        int textWidth = metrics.stringWidth(text);
        int textHeight = metrics.getAscent() + metrics.getDescent();
        BadgeMetrics badge = new BadgeMetrics(
                textWidth + BADGE_HORIZONTAL_PADDING * 2,
                textHeight + BADGE_VERTICAL_PADDING * 2
        );
        int labelRadius = layout.outerRadius() + 24;

        int rawX = (int) Math.round(layout.centerX() + labelRadius * Math.cos(radians));
        int rawY = (int) Math.round(layout.centerY() - labelRadius * Math.sin(radians));

        int minCenterX = CANVAS_MARGIN + badge.width() / 2;
        int maxCenterX = Math.max(
                minCenterX,
                layout.legendX() - 12 - badge.width() / 2
        );
        int minCenterY = Math.max(
                CANVAS_MARGIN + badge.height() / 2,
                layout.titleHeight() + CANVAS_MARGIN + badge.height() / 2
        );
        int maxCenterY = Math.max(
                minCenterY,
                options.height() - CANVAS_MARGIN - badge.height() / 2
        );
        int labelCenterX = clamp(rawX, minCenterX, maxCenterX);
        int labelCenterY = clamp(rawY, minCenterY, maxCenterY);

        double anchorX = layout.centerX() + layout.outerRadius() * Math.cos(radians);
        double anchorY = layout.centerY() - layout.outerRadius() * Math.sin(radians);
        int horizontalDirection = Math.cos(radians) < 0 ? -1 : 1;
        if (rawY < minCenterY || rawY > maxCenterY) {
            int sideX = (int) Math.round(
                    anchorX + horizontalDirection * (badge.width() / 2.0 + 9)
            );
            labelCenterX = clamp(sideX, minCenterX, maxCenterX);
        }
        labelCenterX = pushOutsideRing(
                layout.centerX(),
                layout.centerY(),
                labelCenterX,
                labelCenterY,
                badge.width(),
                layout.outerRadius(),
                minCenterX,
                maxCenterX,
                horizontalDirection
        );

        drawLeader(
                graphics,
                layout,
                badge.width(),
                labelCenterX,
                labelCenterY,
                anchorX,
                anchorY,
                radians
        );
        drawBadge(graphics, text, labelCenterX, labelCenterY, metrics, badge);
    }

    private void drawLeader(
            Graphics2D graphics,
            ChartLayout layout,
            int badgeWidth,
            int labelCenterX,
            int labelCenterY,
            double anchorX,
            double anchorY,
            double radians
    ) {
        double elbowX = layout.centerX() + (layout.outerRadius() + 10) * Math.cos(radians);
        double elbowY = layout.centerY() - (layout.outerRadius() + 10) * Math.sin(radians);
        int lineEndX = labelCenterX < anchorX
                ? labelCenterX + badgeWidth / 2 + LEADER_BADGE_GAP
                : labelCenterX - badgeWidth / 2 - LEADER_BADGE_GAP;

        graphics.setStroke(new BasicStroke(1.4f));
        graphics.setColor(theme.leaderColor());
        Path2D.Double leader = new Path2D.Double();
        leader.moveTo(anchorX, anchorY);
        leader.lineTo(elbowX, elbowY);
        leader.lineTo(lineEndX, labelCenterY);
        graphics.draw(leader);
    }

    private void drawBadge(
            Graphics2D graphics,
            String text,
            int centerX,
            int centerY,
            FontMetrics metrics,
            BadgeMetrics badge
    ) {
        int x = centerX - badge.width() / 2;
        int y = centerY - badge.height() / 2;

        graphics.setColor(theme.backgroundColor());
        graphics.fillRoundRect(x, y, badge.width(), badge.height(), 8, 8);
        graphics.setStroke(new BasicStroke(1.0f));
        graphics.setColor(theme.badgeBorderColor());
        graphics.drawRoundRect(x, y, badge.width(), badge.height(), 8, 8);

        graphics.setColor(theme.textColor());
        TextDrawing.drawCentered(graphics, text, centerX, centerY, metrics);
    }

    private static int pushOutsideRing(
            int centerX,
            int centerY,
            int labelCenterX,
            int labelCenterY,
            int badgeWidth,
            int outerRadius,
            int minCenterX,
            int maxCenterX,
            int horizontalDirection
    ) {
        double deltaY = labelCenterY - centerY;
        double safeRadius = outerRadius + badgeWidth / 2.0 + BADGE_RING_GAP;
        if (Math.hypot(labelCenterX - centerX, deltaY) >= safeRadius) {
            return labelCenterX;
        }

        double requiredDeltaX = Math.abs(deltaY) >= safeRadius
                ? 0.0
                : Math.sqrt(safeRadius * safeRadius - deltaY * deltaY);
        int shiftedX = (int) Math.round(centerX + horizontalDirection * requiredDeltaX);
        return clamp(shiftedX, minCenterX, maxCenterX);
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private record BadgeMetrics(int width, int height) {
    }
}

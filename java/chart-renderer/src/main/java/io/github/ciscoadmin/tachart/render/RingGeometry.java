package io.github.ciscoadmin.tachart.render;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

final class RingGeometry {
    private RingGeometry() {
    }

    static Shape ring(int centerX, int centerY, int outerRadius, int innerRadius) {
        Area area = new Area(new Ellipse2D.Double(
                centerX - outerRadius,
                centerY - outerRadius,
                outerRadius * 2.0,
                outerRadius * 2.0
        ));
        area.subtract(new Area(new Ellipse2D.Double(
                centerX - innerRadius,
                centerY - innerRadius,
                innerRadius * 2.0,
                innerRadius * 2.0
        )));
        return area;
    }

    static Shape segment(
            int centerX,
            int centerY,
            int outerRadius,
            int innerRadius,
            double startDegrees,
            double clockwiseSweepDegrees
    ) {
        int steps = Math.max(8, (int) Math.ceil(clockwiseSweepDegrees / 2.0));
        Path2D.Double path = new Path2D.Double();

        for (int index = 0; index <= steps; index++) {
            double angle = startDegrees - clockwiseSweepDegrees * index / steps;
            double radians = Math.toRadians(angle);
            double x = centerX + outerRadius * Math.cos(radians);
            double y = centerY - outerRadius * Math.sin(radians);
            if (index == 0) {
                path.moveTo(x, y);
            } else {
                path.lineTo(x, y);
            }
        }

        for (int index = steps; index >= 0; index--) {
            double angle = startDegrees - clockwiseSweepDegrees * index / steps;
            double radians = Math.toRadians(angle);
            double x = centerX + innerRadius * Math.cos(radians);
            double y = centerY - innerRadius * Math.sin(radians);
            path.lineTo(x, y);
        }

        path.closePath();
        return path;
    }
}

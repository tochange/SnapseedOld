package com.niksoftware.snapseed.views;

import android.graphics.PointF;
import com.niksoftware.snapseed.util.Geometry;
import com.niksoftware.snapseed.util.Geometry.Line;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

final class CropRotateViewHelper {
    public static final int INDEX_NOT_FOUND = -1;
    private static final int MATRIX_ELEMENT_COUNT = 9;

    private CropRotateViewHelper() {
    }

    public static List<Line> buildEdgeLoop(float[] vertices) {
        int coordCount = vertices == null ? 0 : vertices.length;
        if (coordCount < 2 || coordCount % 2 != 0) {
            throw new InvalidParameterException();
        }
        List<Line> result = new ArrayList(coordCount / 2);
        for (int i = 0; i < coordCount; i += 2) {
            int nextI = (i + 2) % coordCount;
            result.add(new Line(vertices[i], vertices[i + 1], vertices[nextI], vertices[nextI + 1]));
        }
        return result;
    }

    public static boolean isPointInsidePoly(List<Line> poly, float x, float y) {
        return getMismatchPolyEdgeIndex(poly, x, y) < 0;
    }

    public static int getMismatchPolyEdgeIndex(List<Line> poly, float x, float y) {
        for (int i = 0; i < poly.size(); i++) {
            if (((Line) poly.get(i)).apply(x, y) > 0.0f) {
                return i;
            }
        }
        return -1;
    }

    public static float interpolateValue(float v1, float v2, float fraction) {
        return ((v2 - v1) * fraction) + v1;
    }

    public static void interpolateMatrix(float[] output, float[] start, float[] end, float fraction) {
        if (output.length == 9 && start.length == 9 && end.length == 9) {
            for (int i = 0; i < 9; i++) {
                output[i] = interpolateValue(start[i], end[i], fraction);
            }
            return;
        }
        throw new InvalidParameterException("Invalid matrix size.");
    }

    public static float getIntersectionDistance(Line pline1, Line pline2, Line cline) {
        PointF cross1 = Line.getIntersection(pline1, cline);
        PointF cross2 = Line.getIntersection(pline2, cline);
        return Geometry.distance(cross1.x, cross1.y, cross2.x, cross2.y);
    }
}

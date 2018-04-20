package com.niksoftware.snapseed.util;

import android.graphics.PointF;
import java.util.Locale;

public final class Geometry {

    public static class Line {
        public final float a;
        public final float b;
        public final float c;

        public Line(float a, float b, float c) {
            this.a = a;
            this.b = b;
            this.c = c;
        }

        public Line(float x1, float y1, float x2, float y2) {
            this(y2 - y1, x1 - x2, ((y1 - y2) * x1) + ((x2 - x1) * y1));
        }

        public Line(PointF p1, PointF p2) {
            this(p1.x, p1.y, p2.x, p2.y);
        }

        public Vector getDirection() {
            return new Vector(-this.b, this.a);
        }

        public float apply(float x, float y) {
            return ((this.a * x) + (this.b * y)) + this.c;
        }

        public float apply(PointF point) {
            return apply(point.x, point.y);
        }

        public Line normalize() {
            float ratio = (float) Math.sqrt((double) ((this.a * this.a) + (this.b * this.b)));
            return ratio == 1.0f ? this : new Line(this.a / ratio, this.b / ratio, this.c / ratio);
        }

        public static PointF getIntersection(Line line1, Line line2) {
            float divX = (line1.a * line2.b) - (line2.a * line1.b);
            if (divX == 0.0f) {
                return null;
            }
            float divY = (line2.a * line1.b) - (line1.a * line2.b);
            if (divY != 0.0f) {
                return new PointF(((line1.b * line2.c) - (line2.b * line1.c)) / divX, ((line1.a * line2.c) - (line2.a * line1.c)) / divY);
            }
            return null;
        }

        public static float getCos(Line line1, Line line2) {
            return ((line1.a * line2.a) + (line1.b * line2.b)) / (Vector.length(line1.a, line1.b) * Vector.length(line2.a, line2.b));
        }

        public boolean equals(Object object) {
            if (!(object instanceof Line)) {
                return false;
            }
            Line that = (Line) object;
            if (this.a == that.a && this.b == that.b && this.c == that.c) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return ((Float.floatToIntBits(this.a) * 54) + (Float.floatToIntBits(this.b) * 31)) + Float.floatToIntBits(this.c);
        }

        public String toString() {
            return String.format(Locale.US, "{ a:%.1f, b:%.1f, c:%.1f }", new Object[]{Float.valueOf(this.a), Float.valueOf(this.b), Float.valueOf(this.c)});
        }
    }

    public static class Vector {
        private static final Vector ZERO_VECTOR = new Vector(0.0f, 0.0f);
        public final float a;
        public final float b;

        private Vector(float a, float b) {
            this.a = a;
            this.b = b;
        }

        public static Vector buildVector(float x1, float y1, float x2, float y2) {
            return new Vector(x2 - x1, y2 - y1);
        }

        public static Vector buildVector(PointF p1, PointF p2) {
            return buildVector(p1.x, p1.y, p2.x, p2.y);
        }

        public static Vector buildVector(float a, float b) {
            return (a == 0.0f && b == 0.0f) ? ZERO_VECTOR : new Vector(a, b);
        }

        public static Vector buildNormalizedVector(float a, float b) {
            float len = length(a, b);
            return len > 0.0f ? new Vector(a / len, b / len) : ZERO_VECTOR;
        }

        public Vector normalize() {
            return buildNormalizedVector(this.a, this.b);
        }

        public Vector scale(float scale) {
            return buildVector(this.a * scale, this.b * scale);
        }

        public float length() {
            return length(this.a, this.b);
        }

        public static float length(float a, float b) {
            return (float) Math.sqrt((double) ((a * a) + (b * b)));
        }

        public boolean equals(Object object) {
            if (!(object instanceof Vector)) {
                return false;
            }
            Vector that = (Vector) object;
            if (this.a == that.a && this.b == that.b) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return (Float.floatToIntBits(this.a) * 54) + (Float.floatToIntBits(this.b) * 31);
        }

        public String toString() {
            return String.format(Locale.US, "{ a:%.1f, b:%.1f }", new Object[]{Float.valueOf(this.a), Float.valueOf(this.b)});
        }
    }

    private Geometry() {
    }

    public static float distance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((double) distance2(x1, y1, x2, y2));
    }

    public static float distance2(float x1, float y1, float x2, float y2) {
        return ((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1));
    }

    public static float distance(Line line, float x, float y) {
        return (float) Math.sqrt((double) distance2(line, x, y));
    }

    public static float distance2(Line line, float x, float y) {
        float t = ((line.a * x) + (line.b * y)) + line.c;
        return (t * t) / ((line.a * line.a) + (line.b * line.b));
    }
}

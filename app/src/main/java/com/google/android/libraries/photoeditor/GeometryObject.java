package com.google.android.libraries.photoeditor;

public abstract class GeometryObject {

    public static class Ellipse extends GeometryObject {
        public final float angle;
        public final float posX;
        public final float posY;
        public final float radiusX;
        public final float radiusY;

        public Ellipse(float posX, float posY, float radiusX, float radiusY, float angle) {
            this.posX = posX;
            this.posY = posY;
            this.radiusX = radiusX;
            this.radiusY = radiusY;
            this.angle = angle;
        }

        public Ellipse copy() {
            return new Ellipse(this.posX, this.posY, this.radiusX, this.radiusY, this.angle);
        }

        public void GLRender(int screenWidth, int screenHeight) {
            NativeCore.drawOverlayEllipse(this.posX, this.posY, this.radiusX, this.radiusY, this.angle, screenWidth, screenHeight);
        }
    }

    public static class Line extends GeometryObject {
        public final float beginX;
        public final float beginY;
        public final float endX;
        public final float endY;

        public Line(float beginX, float beginY, float endX, float endY) {
            this.beginX = beginX;
            this.beginY = beginY;
            this.endX = endX;
            this.endY = endY;
        }

        public Line copy() {
            return new Line(this.beginX, this.beginY, this.endX, this.endY);
        }

        public void GLRender(int screenWidth, int screenHeight) {
            NativeCore.drawOverlayLine(this.beginX, this.beginY, this.endX, this.endY, screenWidth, screenHeight);
        }
    }

    public abstract void GLRender(int i, int i2);

    public abstract GeometryObject copy();
}

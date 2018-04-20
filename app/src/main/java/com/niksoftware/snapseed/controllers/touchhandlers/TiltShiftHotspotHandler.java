package com.niksoftware.snapseed.controllers.touchhandlers;

import android.graphics.Matrix;
import android.view.View;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.UndoManager;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.core.filterparameters.TiltAndShiftFilterParameter;
import com.niksoftware.snapseed.core.rendering.GeometryObject;
import com.niksoftware.snapseed.core.rendering.GeometryObject.Ellipse;
import com.niksoftware.snapseed.core.rendering.GeometryObject.Line;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.WorkingAreaView;
import java.util.ArrayList;

public class TiltShiftHotspotHandler extends HotspotHandlerBase {
    private static final float RADIANS_TO_DEGREES_RATIO = 57.295776f;
    private NotificationCenterListener _didChangeFilterParameterValue;
    private NotificationCenterListener _didEnterEditingScreenListener;
    private int _lastXRadius = 0;
    private int _lastYRadius = 0;
    private float _pinchAngleDiff = 0.0f;
    private NotificationCenterListener _undoRedoPerformed;

    public TiltShiftHotspotHandler() {
        NotificationCenter center = NotificationCenter.getInstance();
        NotificationCenterListener anonymousClass1 = new NotificationCenterListener() {
            public void performAction(Object arg) {
                TiltShiftHotspotHandler.this.showCircle(true);
                TiltShiftHotspotHandler.this.showHotspot(true);
                TiltShiftHotspotHandler.this.hideHotspotDelayed();
            }
        };
        this._didEnterEditingScreenListener = anonymousClass1;
        center.addListener(anonymousClass1, ListenerType.DidEnterEditingScreen);
        anonymousClass1 = new NotificationCenterListener() {
            public void performAction(Object arg) {
                if (arg != null) {
                    Integer filterParameterType = (Integer) arg;
                    if (filterParameterType.intValue() == 17 || filterParameterType.intValue() == 3) {
                        TiltShiftHotspotHandler.this.showCircle(true);
                        TiltShiftHotspotHandler.this.showHotspot(true);
                        TiltShiftHotspotHandler.this.hideHotspotDelayed();
                    }
                }
            }
        };
        this._didChangeFilterParameterValue = anonymousClass1;
        center.addListener(anonymousClass1, ListenerType.DidChangeFilterParameterValue);
        anonymousClass1 = new NotificationCenterListener() {
            public void performAction(Object arg) {
                TiltShiftHotspotHandler.this.updateHotspotPosition();
                TiltShiftHotspotHandler.this.showCircle(true);
                TiltShiftHotspotHandler.this.hideHotspotDelayed();
            }
        };
        this._undoRedoPerformed = anonymousClass1;
        center.addListener(anonymousClass1, ListenerType.UndoRedoPerformed);
    }

    public void cleanup() {
        NotificationCenter notificationCenter = NotificationCenter.getInstance();
        notificationCenter.removeListener(this._didEnterEditingScreenListener, ListenerType.DidEnterEditingScreen);
        notificationCenter.removeListener(this._didChangeFilterParameterValue, ListenerType.DidChangeFilterParameterValue);
        notificationCenter.removeListener(this._undoRedoPerformed, ListenerType.UndoRedoPerformed);
        super.cleanup();
    }

    private float normPosToViewport(double i) {
        return (((float) i) * 2.0f) - 1.0f;
    }

    private float normPosToViewportInv(double i) {
        return ((float) (1.0d + i)) / 2.0f;
    }

    protected void showCircle(boolean visible) {
        WorkingAreaView workingAreaView = MainActivity.getWorkingAreaView();
        View imageView = workingAreaView.getImageView();
        if (!visible || imageView == null) {
            workingAreaView.clearGeometryObjects();
        } else {
            float whFac;
            TiltAndShiftFilterParameter filterParameter = (TiltAndShiftFilterParameter) MainActivity.getFilterParameter();
            float rotation = ((float) filterParameter.getParameterValueOld(18)) / 1.0E7f;
            float transition = ((float) filterParameter.getParameterValueOld(17)) / 100.0f;
            float centerX = (float) FilterParameter.lowBitsAsDouble(filterParameter.getParameterValueOld(24));
            float centerY = (float) FilterParameter.lowBitsAsDouble(filterParameter.getParameterValueOld(25));
            float xAxis = ((float) filterParameter.getXAxis()) / 100.0f;
            float yAxis = ((float) filterParameter.getYAxis()) / 100.0f;
            ArrayList<GeometryObject> geometryObjects = new ArrayList();
            int imageWidth = imageView.getWidth();
            int imageHeight = imageView.getHeight();
            float factorX = ((float) imageWidth) / ((float) imageHeight);
            float factorY = ((float) imageHeight) / ((float) imageWidth);
            if (imageWidth >= imageHeight) {
                whFac = (float) imageWidth;
            } else {
                whFac = (float) imageHeight;
            }
            if (filterParameter.getParameterValueOld(3) == 0) {
                float xScale = whFac / ((float) imageWidth);
                float yScale = whFac / ((float) imageHeight);
                float vpY = -yAxis;
                double cosRotation = Math.cos((double) rotation);
                double sinRotation = Math.sin((double) rotation);
                float xMid = normPosToViewport((double) centerX);
                float yMid = normPosToViewport((double) centerY);
                geometryObjects.add(new Line(normPosToViewportInv((((-8.0d * cosRotation) - (((double) vpY) * sinRotation)) * ((double) xScale)) + ((double) xMid)), normPosToViewportInv((((-8.0d * sinRotation) + (((double) vpY) * cosRotation)) * ((double) yScale)) + ((double) yMid)), normPosToViewportInv((((8.0d * cosRotation) - (((double) vpY) * sinRotation)) * ((double) xScale)) + ((double) xMid)), normPosToViewportInv((((8.0d * sinRotation) + (((double) vpY) * cosRotation)) * ((double) yScale)) + ((double) yMid))));
                vpY = yAxis;
                geometryObjects.add(new Line(normPosToViewportInv((((-8.0d * cosRotation) - (((double) vpY) * sinRotation)) * ((double) xScale)) + ((double) xMid)), normPosToViewportInv((((-8.0d * sinRotation) + (((double) vpY) * cosRotation)) * ((double) yScale)) + ((double) yMid)), normPosToViewportInv((((8.0d * cosRotation) - (((double) vpY) * sinRotation)) * ((double) xScale)) + ((double) xMid)), normPosToViewportInv((((8.0d * sinRotation) + (((double) vpY) * cosRotation)) * ((double) yScale)) + ((double) yMid))));
                vpY = (-yAxis) - (transition / 2.0f);
                geometryObjects.add(new Line(normPosToViewportInv((((-8.0d * cosRotation) - (((double) vpY) * sinRotation)) * ((double) xScale)) + ((double) xMid)), normPosToViewportInv((((-8.0d * sinRotation) + (((double) vpY) * cosRotation)) * ((double) yScale)) + ((double) yMid)), normPosToViewportInv((((8.0d * cosRotation) - (((double) vpY) * sinRotation)) * ((double) xScale)) + ((double) xMid)), normPosToViewportInv((((8.0d * sinRotation) + (((double) vpY) * cosRotation)) * ((double) yScale)) + ((double) yMid))));
                vpY = yAxis + (transition / 2.0f);
                geometryObjects.add(new Line(normPosToViewportInv((((-8.0d * cosRotation) - (((double) vpY) * sinRotation)) * ((double) xScale)) + ((double) xMid)), normPosToViewportInv((((-8.0d * sinRotation) + (((double) vpY) * cosRotation)) * ((double) yScale)) + ((double) yMid)), normPosToViewportInv((((8.0d * cosRotation) - (((double) vpY) * sinRotation)) * ((double) xScale)) + ((double) xMid)), normPosToViewportInv((((8.0d * sinRotation) + (((double) vpY) * cosRotation)) * ((double) yScale)) + ((double) yMid))));
            } else {
                float xyFac;
                Matrix m = new Matrix();
                float[] inCoord;
                float[] outCoord;
                float[] inCoord2;
                float[] outCoord2;
                if (imageWidth > imageHeight) {
                    inCoord = new float[]{(centerX - (xAxis / 2.0f)) * factorX, centerY, ((xAxis / 2.0f) + centerX) * factorX, centerY};
                    outCoord = new float[inCoord.length];
                    m.setRotate(RADIANS_TO_DEGREES_RATIO * rotation, factorX * centerX, centerY);
                    m.mapPoints(outCoord, inCoord);
                    geometryObjects.add(new Line(outCoord[0] / factorX, outCoord[1], outCoord[2] / factorX, outCoord[3]));
                    inCoord2 = new float[]{factorX * centerX, centerY - ((yAxis / 2.0f) * factorX), factorX * centerX, ((yAxis / 2.0f) * factorX) + centerY};
                    outCoord2 = new float[inCoord.length];
                    m.setRotate(RADIANS_TO_DEGREES_RATIO * rotation, factorX * centerX, centerY);
                    m.mapPoints(outCoord2, inCoord2);
                    geometryObjects.add(new Line(outCoord2[0] / factorX, outCoord2[1], outCoord2[2] / factorX, outCoord2[3]));
                } else {
                    inCoord = new float[]{centerX - ((xAxis / 2.0f) * factorY), factorY * centerY, ((xAxis / 2.0f) * factorY) + centerX, factorY * centerY};
                    outCoord = new float[inCoord.length];
                    m.setRotate(RADIANS_TO_DEGREES_RATIO * rotation, centerX, factorY * centerY);
                    m.mapPoints(outCoord, inCoord);
                    geometryObjects.add(new Line(outCoord[0], outCoord[1] / factorY, outCoord[2], outCoord[3] / factorY));
                    inCoord2 = new float[]{factorX * centerX, centerY - (yAxis / 2.0f), factorX * centerX, (yAxis / 2.0f) + centerY};
                    outCoord2 = new float[inCoord.length];
                    m.setRotate(RADIANS_TO_DEGREES_RATIO * rotation, factorX * centerX, centerY);
                    m.mapPoints(outCoord2, inCoord2);
                    geometryObjects.add(new Line(outCoord2[0] / factorX, outCoord2[1], outCoord2[2] / factorX, outCoord2[3]));
                }
                geometryObjects.add(new Ellipse(centerX, centerY, xAxis, yAxis, rotation));
                float norm = (float) Math.sqrt((double) ((xAxis * xAxis) + (yAxis * yAxis)));
                float xNorm = xAxis / norm;
                float yNorm = yAxis / norm;
                if (xNorm >= yNorm) {
                    xyFac = xNorm;
                } else {
                    xyFac = yNorm;
                }
                ArrayList<GeometryObject> arrayList = geometryObjects;
                arrayList.add(new Ellipse(centerX, centerY, xAxis + ((transition * xNorm) * xyFac), yAxis + ((transition * yNorm) * xyFac), rotation));
            }
            workingAreaView.setGeometryObjects(geometryObjects);
        }
        workingAreaView.requestRender();
    }

    protected boolean circleOnWhenTouchUp() {
        return true;
    }

    public boolean handlePinchBegin(int x, int y, float size, float arc) {
        MainActivity.getEditingToolbar().setCompareEnabled(false);
        FilterParameter filterParameter = MainActivity.getFilterParameter();
        this._pinchAngleDiff = arc - (((float) filterParameter.getParameterValueOld(18)) / 1.0E7f);
        this._pinchAngleDiff %= 3.1415927f;
        this._pinchCreateUndo = true;
        this._pinchSize = size;
        this._lastXRadius = filterParameter.getParameterValueOld(201);
        this._lastYRadius = filterParameter.getParameterValueOld(202);
        showHotspot(true);
        showCircle(true);
        MainActivity.getWorkingAreaView().requestRender();
        return true;
    }

    public void handlePinchEnd() {
        hideHotspotDelayed();
        MainActivity.getWorkingAreaView().requestRender();
        MainActivity.getEditingToolbar().setCompareEnabled(true);
    }

    public void handlePinchAbort() {
        showHotspot(false);
        MainActivity.getEditingToolbar().setCompareEnabled(true);
    }

    public boolean handlePinch(int x, int y, float size, float arc) {
        int xValue;
        int yValue;
        FilterParameter filterParameter = MainActivity.getFilterParameter();
        if (this._pinchCreateUndo) {
            UndoManager.getUndoManager().createUndo(filterParameter, 4, true);
            this._pinchCreateUndo = false;
        }
        filterParameter.setParameterValueOld(18, (int) (1.0E7f * ((arc - this._pinchAngleDiff) % 3.1415927f)));
        float scale = size / this._pinchSize;
        if (scale == 1.0f) {
            xValue = this._lastXRadius;
            yValue = this._lastYRadius;
        } else {
            float yScale = (float) Math.sin((double) Math.abs(this._pinchAngleDiff));
            float xScale = 1.0f - yScale;
            if (scale > 1.0f) {
                xValue = (int) ((((float) this._lastXRadius) + (30.0f * (1.0f + ((scale - 1.0f) * xScale)))) - 30.0f);
                yValue = (int) ((((float) this._lastYRadius) + (30.0f * (1.0f + ((scale - 1.0f) * yScale)))) - 30.0f);
            } else {
                xValue = (int) ((((float) this._lastXRadius) - (30.0f / (1.0f - ((1.0f - scale) * xScale)))) + 30.0f);
                yValue = (int) ((((float) this._lastYRadius) - (30.0f / (1.0f - ((1.0f - scale) * yScale)))) + 30.0f);
            }
        }
        if (filterParameter.getParameterValueOld(3) == 0) {
            xValue = yValue;
        }
        filterParameter.setParameterValueOld(201, xValue);
        filterParameter.setParameterValueOld(202, yValue);
        TrackerData.getInstance().usingParameter(18, filterParameter.isDefaultParameter(18));
        NotificationCenter.getInstance().performAction(ListenerType.DidChangeFilterParameterValue, null);
        showCircle(true);
        MainActivity.getWorkingAreaView().requestRender();
        return true;
    }
}

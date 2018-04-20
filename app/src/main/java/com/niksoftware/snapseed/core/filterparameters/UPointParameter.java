package com.niksoftware.snapseed.core.filterparameters;

import android.graphics.Rect;
import com.niksoftware.snapseed.MainActivity;

public class UPointParameter extends FilterParameter {
    private static final int[] UPOINT_PARAM_TYPE = new int[]{0, 1, 2, 501, 502, 4, 201, 202, 203};

    protected int[] getAutoParams() {
        return UPOINT_PARAM_TYPE;
    }

    public int getDefaultValue(int param) {
        return param == 4 ? 50 : 0;
    }

    public int getMaxValue(int param) {
        switch (param) {
            case 201:
                return Integer.MAX_VALUE;
            case 501:
            case 502:
                return 65535;
            default:
                return 100;
        }
    }

    public int getMinValue(int param) {
        switch (param) {
            case 201:
                return Integer.MIN_VALUE;
            case 501:
            case 502:
                return 0;
            default:
                return -100;
        }
    }

    public int getDefaultParameter() {
        return 0;
    }

    public int getFilterType() {
        return 300;
    }

    private Rect getImageViewRect() {
        return MainActivity.getWorkingAreaView().getImageViewRect();
    }

    public void setViewXY(int x, int y) {
        Rect viewRect = getImageViewRect();
        int imageWidth = MainActivity.getWorkingAreaView().getImageWidth();
        int imageHeight = MainActivity.getWorkingAreaView().getImageHeight();
        y = (imageHeight * y) / viewRect.height();
        x = Math.max(0, Math.min((imageWidth * x) / viewRect.width(), imageWidth - 1));
        y = Math.max(0, Math.min(y, imageHeight - 1));
        setParameterValueOld(501, x);
        setParameterValueOld(502, y);
        updateRGBA(this);
    }

    public int getViewX() {
        return (getImageViewRect().width() * getParameterValueOld(501)) / MainActivity.getWorkingAreaView().getImageWidth();
    }

    public int getViewY() {
        return (getImageViewRect().height() * getParameterValueOld(502)) / MainActivity.getWorkingAreaView().getImageHeight();
    }

    public int getX() {
        return getParameterValueOld(501);
    }

    public int getY() {
        return getParameterValueOld(502);
    }

    public void setCenterSize(int size) {
        setParameterValueOld(4, size);
    }

    public int getCenterSize() {
        return getParameterValueOld(4);
    }

    public void setRGBA(int rgba) {
        setParameterValueOld(201, rgba);
    }

    public int getRGBA() {
        return getParameterValueOld(201);
    }

    public void setInking(boolean inkingOn) {
        setParameterValueOld(202, inkingOn ? 1 : 0);
    }

    public boolean isInking() {
        return getParameterValueOld(202) != 0;
    }

    public void setSelected(boolean isSelected) {
        setParameterValueOld(203, isSelected ? 1 : 0);
    }

    public boolean isSelected() {
        return getParameterValueOld(203) != 0;
    }

    protected void updateRGBA(UPointParameter param) {
        param.setRGBA(MainActivity.getWorkingAreaView().getRGBA(param.getX(), param.getY()));
    }
}

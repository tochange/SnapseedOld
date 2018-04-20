package com.google.android.libraries.photoeditor.filterparameters;

import android.content.Context;

public class TiltAndShiftFilterParameter extends FilterParameter {
    public int getFilterType() {
        return 14;
    }

    protected int[] getAutoParams() {
        return new int[]{19, 5, 24, 25, 17, 2, 1, 0, 3, 18, 201, 202};
    }

    public int getDefaultValue(int param) {
        switch (param) {
            case 1:
                return 25;
            case 5:
            case 24:
            case 25:
                return FilterParameter.packDouble2LowerInt(0.5d);
            case 17:
            case 19:
                return 50;
            case 201:
            case 202:
                return 10;
            default:
                return 0;
        }
    }

    public int getMaxValue(int param) {
        switch (param) {
            case 3:
                return 1;
            case 5:
            case 18:
            case 24:
            case 25:
                return Integer.MAX_VALUE;
            case 17:
                return 100;
            default:
                return 100;
        }
    }

    public int getMinValue(int param) {
        switch (param) {
            case 0:
                return -100;
            case 2:
                return -100;
            case 5:
            case 24:
            case 25:
                return Integer.MIN_VALUE;
            case 17:
                return 0;
            case 18:
                return Integer.MIN_VALUE;
            case 201:
            case 202:
                return 2;
            default:
                return 0;
        }
    }

    public int getDefaultParameter() {
        return 17;
    }

    public String getParameterDescription(Context context, int parameterType, Object parameterValue) {
        String description = null;
        switch (parameterType) {
            case 3:
                description = getStyleDescription(context, ((Integer) parameterValue).intValue());
                break;
            case 17:
            case 18:
                description = "";
                break;
        }
        return description != null ? description : super.getParameterDescription(context, parameterType, parameterValue);
    }

    public int getXAxis() {
        return getParameterValueOld(201);
    }

    public int getYAxis() {
        return getParameterValueOld(202);
    }

    public boolean setXAxis(int paramValue) {
        return setParameterValueOld(201, paramValue);
    }

    public boolean setYAxis(int paramValue) {
        return setParameterValueOld(202, paramValue);
    }

    private String getStyleDescription(Context context, int styleId) {
        throw new UnsupportedOperationException("Not implemented");
    }
}

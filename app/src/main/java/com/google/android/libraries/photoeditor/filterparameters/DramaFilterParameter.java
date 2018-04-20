package com.google.android.libraries.photoeditor.filterparameters;

import android.content.Context;

public class DramaFilterParameter extends FilterParameter {
    public int getFilterType() {
        return 9;
    }

    protected int[] getAutoParams() {
        return new int[]{3, 12, 2};
    }

    public int getDefaultValue(int param) {
        switch (param) {
            case 2:
                return -40;
            case 3:
                return 0;
            default:
                return 90;
        }
    }

    public int getMaxValue(int param) {
        return param == 3 ? 5 : 100;
    }

    public int getMinValue(int param) {
        return param == 2 ? -100 : 0;
    }

    public int getDefaultParameter() {
        return 12;
    }

    public boolean affectsPanorama() {
        return false;
    }

    public String getParameterDescription(Context context, int parameterType, Object parameterValue) {
        String description = null;
        switch (parameterType) {
            case 3:
                description = getStyleDescription(context, ((Integer) parameterValue).intValue());
                break;
            case 12:
                description = String.format("%s %+d", new Object[]{getParameterTitle(context, parameterType), (Integer) parameterValue});
                break;
        }
        return description != null ? description : super.getParameterDescription(context, parameterType, parameterValue);
    }

    private String getStyleDescription(Context context, int styleId) {
        throw new UnsupportedOperationException("Not implemented");
    }
}

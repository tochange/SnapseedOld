package com.google.android.libraries.photoeditor.filterparameters;

import android.content.Context;

public class CropFilterParameter extends FilterParameter {
    public int getFilterType() {
        return 6;
    }

    protected int[] getAutoParams() {
        return new int[]{42, 41, 40, 43, 44, 45, 46};
    }

    public int getDefaultValue(int param) {
        return 0;
    }

    public int getMaxValue(int param) {
        return param == 42 ? 9 : Integer.MAX_VALUE;
    }

    public int getMinValue(int param) {
        return param == 42 ? 0 : Integer.MIN_VALUE;
    }

    public int getDefaultParameter() {
        return 42;
    }

    public String getParameterDescription(Context context, int parameterType, Object parameterValue) {
        throw new UnsupportedOperationException("Not implemented");
    }
}

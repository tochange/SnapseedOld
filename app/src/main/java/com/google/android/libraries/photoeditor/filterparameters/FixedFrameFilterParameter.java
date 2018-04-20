package com.google.android.libraries.photoeditor.filterparameters;

import android.content.Context;

public class FixedFrameFilterParameter extends FilterParameter {
    public int getFilterType() {
        return 17;
    }

    protected int[] getAutoParams() {
        return new int[]{224, 223, 221, 9, 105, 103, 113, 3};
    }

    public int getDefaultValue(int param) {
        switch (param) {
            case 3:
                return 0;
            case 9:
                return 100;
            case 103:
                return 0;
            case 105:
                return 0;
            case 113:
                return 0;
            case 221:
                return 15;
            case 223:
                return 0;
            case 224:
                return 0;
            default:
                return 0;
        }
    }

    public int getMinValue(int param) {
        switch (param) {
            case 3:
                return 0;
            case 9:
                return 0;
            case 103:
                return Integer.MIN_VALUE;
            case 105:
                return 0;
            case 113:
                return Integer.MIN_VALUE;
            case 221:
                return -100;
            case 223:
                return 0;
            case 224:
                return 0;
            default:
                return 0;
        }
    }

    public int getMaxValue(int param) {
        switch (param) {
            case 3:
                return 1;
            case 9:
                return 100;
            case 103:
                return Integer.MAX_VALUE;
            case 105:
                return 3;
            case 113:
                return Integer.MAX_VALUE;
            case 221:
                return 100;
            case 223:
                return 22;
            case 224:
                return 1;
            default:
                return 0;
        }
    }

    public int getDefaultParameter() {
        return 1000;
    }

    public String getParameterDescription(Context context, int parameterType, Object parameterValue) {
        throw new UnsupportedOperationException("Not implemented");
    }
}

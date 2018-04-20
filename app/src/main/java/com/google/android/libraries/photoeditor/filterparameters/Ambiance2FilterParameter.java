package com.google.android.libraries.photoeditor.filterparameters;

import android.content.Context;
import com.google.android.libraries.photoeditor.NativeCore;

public class Ambiance2FilterParameter extends FilterParameter {
    private static final int[] FILTER_PARAMETERS = new int[]{12, 0, 2, 650, 656, 651, 652, 653, 3};

    public int getFilterType() {
        return 100;
    }

    protected int[] getAutoParams() {
        return FILTER_PARAMETERS;
    }

    public int getDefaultValue(int param) {
        switch (param) {
            case 0:
            case 2:
            case 3:
            case 656:
                return 0;
            case 12:
                return 85;
            case 650:
                return 50;
            case 651:
                return 60;
            case 652:
                return 50;
            case 653:
                return 50;
            default:
                throw new IllegalArgumentException();
        }
    }

    public int getMinValue(int param) {
        switch (param) {
            case 0:
                return -100;
            case 2:
                return -100;
            case 3:
            case 12:
            case 650:
            case 651:
            case 652:
            case 653:
            case 656:
                return 0;
            default:
                throw new IllegalArgumentException();
        }
    }

    public int getMaxValue(int param) {
        switch (param) {
            case 0:
            case 2:
            case 12:
            case 650:
            case 651:
            case 652:
            case 653:
            case 656:
                return 100;
            case 3:
                return NativeCore.getMaxValue(100, 3);
            default:
                throw new IllegalArgumentException();
        }
    }

    public int getDefaultParameter() {
        return 12;
    }

    public String getParameterTitle(Context context, int parameterType) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public String getParameterDescription(Context context, int parameterType, Object parameterValue) {
        throw new UnsupportedOperationException("Not implemented");
    }
}

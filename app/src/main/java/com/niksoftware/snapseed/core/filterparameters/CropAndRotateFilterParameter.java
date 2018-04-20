package com.niksoftware.snapseed.core.filterparameters;

import android.content.Context;

public class CropAndRotateFilterParameter extends FilterParameter {
    private static final int[] FILTER_PARAMETERS = new int[]{42, 41, 40, 43, 44, 45, 46};

    public int getFilterType() {
        return 20;
    }

    protected int[] getAutoParams() {
        return FILTER_PARAMETERS;
    }

    public int getDefaultValue(int param) {
        return param == 42 ? 1 : 0;
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
        if (parameterType != 42) {
            return super.getParameterDescription(context, parameterType, parameterValue);
        }
        if (parameterValue == null) {
            return "*UNKNOWN*";
        }
        switch (((Integer) parameterValue).intValue()) {
            case 0:
                return context.getString(R.string.crop_free);
            case 1:
                return context.getString(R.string.crop_original);
            case 2:
                return context.getString(R.string.crop_1x1);
            case 3:
                return context.getString(R.string.crop_din);
            case 4:
                return context.getString(R.string.crop_3x2);
            case 5:
                return context.getString(R.string.crop_4x3);
            case 6:
                return context.getString(R.string.crop_5x4);
            case 7:
                return context.getString(R.string.crop_7x5);
            case 8:
                return context.getString(R.string.crop_16x9);
            default:
                return "*UNKNOWN*";
        }
    }
}

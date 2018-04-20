package com.niksoftware.snapseed.core.filterparameters;

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
        switch (parameterType) {
            case 9:
                return context.getString(((Integer) parameterValue).intValue() != 0 ? R.string.frame_color_on : R.string.frame_color_off);
            case 103:
            case 113:
                return "";
            case 221:
                return "";
            case 223:
                return String.format("%s %d", new Object[]{context.getString(R.string.frame), Integer.valueOf(((Integer) parameterValue).intValue() + 1)});
            case 224:
                return ((Integer) parameterValue).intValue() == 1 ? context.getString(R.string.crop_1x1) : context.getString(R.string.crop_original);
            default:
                return super.getParameterDescription(context, parameterType, parameterValue);
        }
    }
}

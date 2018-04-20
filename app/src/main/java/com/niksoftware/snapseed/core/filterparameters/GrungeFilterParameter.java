package com.niksoftware.snapseed.core.filterparameters;

import android.content.Context;

public class GrungeFilterParameter extends FilterParameter {
    public int getFilterType() {
        return 10;
    }

    protected int[] getAutoParams() {
        return new int[]{3, 104, 0, 1, 5, 24, 25, 4, 2, 102, 101, 106, 107};
    }

    public int getDefaultValue(int param) {
        switch (param) {
            case 0:
                return 20;
            case 1:
            case 104:
                return 40;
            case 2:
                return 50;
            case 3:
                return 400;
            case 4:
                return 55;
            case 5:
            case 24:
            case 25:
                return FilterParameter.packDouble2LowerInt(0.5d);
            case 101:
                return 101;
            case 102:
                return 1000;
            case 106:
            case 107:
                return 0;
            default:
                return 0;
        }
    }

    public int getMaxValue(int param) {
        switch (param) {
            case 3:
                return 1500;
            case 5:
            case 24:
            case 25:
            case 106:
            case 107:
                return Integer.MAX_VALUE;
            case 101:
                return 105;
            default:
                return 100;
        }
    }

    public int getMinValue(int param) {
        switch (param) {
            case 0:
                return -100;
            case 3:
                return 1;
            case 5:
            case 24:
            case 25:
            case 106:
            case 107:
                return Integer.MIN_VALUE;
            case 101:
                return 101;
            default:
                return 0;
        }
    }

    public int getDefaultParameter() {
        return 3;
    }

    public String getParameterDescription(Context context, int parameterType, Object parameterValue) {
        String description = null;
        switch (parameterType) {
            case 3:
                description = String.format("%s %d", new Object[]{context.getString(R.string.style), (Integer) parameterValue});
                break;
            case 101:
                description = super.getParameterDescription(context, 101, Integer.valueOf(((Integer) parameterValue).intValue() - 101));
                break;
        }
        return description != null ? description : super.getParameterDescription(context, parameterType, parameterValue);
    }
}

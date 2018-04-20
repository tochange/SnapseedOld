package com.niksoftware.snapseed.core.filterparameters;

import android.content.Context;

import com.niksoftware.snapseed.core.NativeCore;

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
                NativeCore.getInstance();
                return NativeCore.getMaxValue(100, 3);
            default:
                throw new IllegalArgumentException();
        }
    }

    public int getDefaultParameter() {
        return 12;
    }

    public String getParameterTitle(Context context, int parameterType) {
        return parameterType == 12 ? context.getString(R.string.param_FilterStrength) : super.getParameterTitle(context, parameterType);
    }

    public String getParameterDescription(Context context, int parameterType, Object parameterValue) {
        switch (parameterType) {
            case 3:
                switch (((Integer) parameterValue).intValue()) {
                    case 0:
                        return context.getString(R.string.gradedit_nature);
                    case 1:
                        return context.getString(R.string.gradedit_people);
                    case 2:
                        return context.getString(R.string.gradedit_fine);
                    case 3:
                        return context.getString(R.string.gradedit_strong);
                    default:
                        break;
                }
            case 12:
            case 650:
            case 651:
            case 652:
            case 653:
            case 656:
                return String.format("%s %+d", new Object[]{getParameterTitle(context, parameterType), (Integer) parameterValue});
        }
        return super.getParameterDescription(context, parameterType, parameterValue);
    }
}

package com.google.android.libraries.photoeditor.filterparameters;

public class VintageFilterParameter extends FilterParameter {
    public int getFilterType() {
        return 8;
    }

    protected int[] getAutoParams() {
        return new int[]{2, 0, 104, 3, 4, 102, 101, 106, 107, 9};
    }

    public int getDefaultValue(int param) {
        switch (param) {
            case 2:
                return 15;
            case 4:
                return 75;
            case 9:
                return 60;
            case 102:
                return 1000;
            case 104:
                return 60;
            default:
                return 0;
        }
    }

    public int getMaxValue(int param) {
        switch (param) {
            case 3:
                return 8;
            case 101:
                return 3;
            case 106:
            case 107:
                return Integer.MAX_VALUE;
            default:
                return 100;
        }
    }

    public int getMinValue(int param) {
        switch (param) {
            case 0:
                return -100;
            case 106:
            case 107:
                return Integer.MIN_VALUE;
            default:
                return 0;
        }
    }

    public int getDefaultParameter() {
        return 0;
    }

    public synchronized boolean setParameterValueOld(int paramKey, int paramValue) {
        boolean result;
        result = super.setParameterValueOld(paramKey, paramValue);
        if (result && paramKey == 3) {
            super.setParameterValueOld(4, vignetteStrengthForStyle(paramValue));
        }
        return result;
    }

    private static int vignetteStrengthForStyle(int style) {
        switch (style) {
            case 0:
            case 3:
            case 6:
                return 75;
            case 1:
                return 50;
            case 2:
                return 25;
            case 4:
                return 50;
            case 5:
                return 25;
            case 7:
                return 50;
            case 8:
                return 25;
            default:
                return 50;
        }
    }
}

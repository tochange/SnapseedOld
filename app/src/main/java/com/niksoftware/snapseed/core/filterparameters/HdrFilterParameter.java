package com.niksoftware.snapseed.core.filterparameters;

public class HdrFilterParameter extends FilterParameter {
    public int getFilterType() {
        return 15;
    }

    protected int[] getAutoParams() {
        return new int[]{12, 1, 2, 231};
    }

    public int getDefaultValue(int param) {
        return param == 1 ? 40 : 0;
    }

    public int getMaxValue(int param) {
        return 100;
    }

    public int getMinValue(int param) {
        if (param == 2 || param == 231) {
            return -100;
        }
        return 0;
    }

    public int getDefaultParameter() {
        return 12;
    }
}

package com.niksoftware.snapseed.core.filterparameters;

public class DetailsFilterParameter extends FilterParameter {
    public int getFilterType() {
        return 13;
    }

    protected int[] getAutoParams() {
        return new int[]{15, 16};
    }

    public int getDefaultValue(int param) {
        return 0;
    }

    public int getMaxValue(int param) {
        return 100;
    }

    public int getMinValue(int param) {
        return 0;
    }

    public int getDefaultParameter() {
        return 15;
    }

    public boolean affectsPanorama() {
        return false;
    }
}

package com.niksoftware.snapseed.core.filterparameters;

public class TuneImageFilterParameter extends FilterParameter {
    public int getFilterType() {
        return 4;
    }

    protected int[] getAutoParams() {
        return new int[]{2, 0, 20, 10, 1, 11};
    }

    public int getDefaultValue(int param) {
        return 0;
    }

    public int getMaxValue(int param) {
        return 100;
    }

    public int getMinValue(int param) {
        if (param == 20) {
            return 0;
        }
        return -100;
    }

    public int getDefaultParameter() {
        return 0;
    }

    public boolean affectsPanorama() {
        return false;
    }
}

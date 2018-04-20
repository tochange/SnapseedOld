package com.google.android.libraries.photoeditor.filterparameters;

public class AutoCorrectFilterParameter extends FilterParameter {
    public int getFilterType() {
        return 2;
    }

    protected int[] getAutoParams() {
        return new int[]{7, 8};
    }

    public int getDefaultValue(int param) {
        return 50;
    }

    public int getMaxValue(int param) {
        return 100;
    }

    public int getMinValue(int param) {
        return 0;
    }

    public int getDefaultParameter() {
        return 7;
    }

    public boolean affectsPanorama() {
        return false;
    }
}

package com.google.android.libraries.photoeditor.filterparameters;

public class StraightenFilterParameter extends FilterParameter {
    public int getFilterType() {
        return 5;
    }

    protected int[] getAutoParams() {
        return new int[]{38, 39};
    }

    public int getDefaultValue(int param) {
        return 0;
    }

    public int getMaxValue(int param) {
        return param == 38 ? 1000 : Integer.MAX_VALUE;
    }

    public int getMinValue(int param) {
        return param == 38 ? -1000 : Integer.MIN_VALUE;
    }

    public int getDefaultParameter() {
        return 38;
    }
}

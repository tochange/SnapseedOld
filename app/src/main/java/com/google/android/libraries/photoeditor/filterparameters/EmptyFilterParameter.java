package com.google.android.libraries.photoeditor.filterparameters;

public class EmptyFilterParameter extends FilterParameter {
    public static final EmptyFilterParameter INSTANCE = new EmptyFilterParameter();

    private EmptyFilterParameter() {
    }

    public int getFilterType() {
        return 1;
    }

    protected int[] getAutoParams() {
        return new int[0];
    }

    public int getDefaultValue(int param) {
        throw new UnsupportedOperationException();
    }

    public int getMaxValue(int param) {
        throw new UnsupportedOperationException();
    }

    public int getMinValue(int param) {
        throw new UnsupportedOperationException();
    }

    public int getDefaultParameter() {
        return 1000;
    }
}

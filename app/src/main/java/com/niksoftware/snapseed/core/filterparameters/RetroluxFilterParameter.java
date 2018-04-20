package com.niksoftware.snapseed.core.filterparameters;

import android.content.Context;
import com.niksoftware.snapseed.core.NativeCore;

public class RetroluxFilterParameter extends FilterParameter {
    public static final int LEAK_TYPE_CRISP = 2;
    public static final int LEAK_TYPE_DYNAMIC = 1;
    public static final int LEAK_TYPE_SOFT = 0;
    public static final int SCRATCH_TYPE_DIRT = 2;
    public static final int SCRATCH_TYPE_FINE = 0;
    public static final int SCRATCH_TYPE_SOFT = 1;

    public int getFilterType() {
        return 16;
    }

    protected int[] getAutoParams() {
        return new int[]{0, 2, 1, 9, 3, 223, 233, 232, 19, 6, 102, 105, 235, 234, 224};
    }

    public int getDefaultValue(int param) {
        NativeCore.getInstance();
        return NativeCore.getDefaultValue(getFilterType(), param);
    }

    public int getMaxValue(int param) {
        NativeCore.getInstance();
        return NativeCore.getMaxValue(getFilterType(), param);
    }

    public int getMinValue(int param) {
        NativeCore.getInstance();
        return NativeCore.getMinValue(getFilterType(), param);
    }

    public int getDefaultParameter() {
        return 0;
    }

    public String getParameterDescription(Context context, int parameterType, Object parameterValue) {
        return (parameterType == 235 || parameterType == 234) ? "" : super.getParameterDescription(context, parameterType, parameterValue);
    }
}

package com.niksoftware.snapseed.core.filterparameters;

import android.content.Context;

import com.niksoftware.snapseed.core.NativeCore;

public class FilmFilterParameter extends FilterParameter {
    private static final int[] AUTO_PARAMETERS = new int[]{9, 0, 2, 19, 14, 6, 3};
    public static final int BW_STYLE = 2;
    public static final int CONTRAST_STYLE = 4;
    public static final int COOL_STYLE = 3;
    private static final int COUNT_OF_STACKS = 6;
    public static final int CROSS_STYLE = 1;
    public static final int VINTAGE_STYLE = 5;
    public static final int WARM_STYLE = 0;

    public int getFilterType() {
        return 200;
    }

    protected int[] getAutoParams() {
        return AUTO_PARAMETERS;
    }

    public int getDefaultValue(int param) {
        return NativeCore.getDefaultValue(getFilterType(), param);
    }

    public int getMaxValue(int param) {
        return NativeCore.getMaxValue(getFilterType(), param);
    }

    public int getMinValue(int param) {
        return NativeCore.getMinValue(getFilterType(), param);
    }

    public int getDefaultParameter() {
        return 9;
    }

    public int[] getParameterValues(int parameterType) {
        int stackCount = getStackCount();
        int[] values = new int[stackCount];
        for (int preset = 0; preset < stackCount; preset++) {
            values[preset] = mapStackNumberToFilterStyle(preset);
        }
        return values;
    }

    public String getParameterDescription(Context context, int parameterType, Object parameterValue) {
        if (parameterType != 3) {
            return super.getParameterDescription(context, parameterType, parameterValue);
        }
        Integer value = (Integer) parameterValue;
        int stack = mapFilterStyleToStackNumber(value.intValue());
        int indexInStack = (value.intValue() - mapStackNumberToFilterStyle(stack)) + 1;
        switch (stack) {
            case 0:
                return String.format("%s %d", new Object[]{context.getString(R.string.film_warm), Integer.valueOf(indexInStack)});
            case 1:
                return String.format("%s %d", new Object[]{context.getString(R.string.film_cross), Integer.valueOf(indexInStack)});
            case 2:
                return String.format("%s %d", new Object[]{context.getString(R.string.film_bw), Integer.valueOf(indexInStack)});
            case 3:
                return String.format("%s %d", new Object[]{context.getString(R.string.film_cool), Integer.valueOf(indexInStack)});
            case 4:
                return String.format("%s %d", new Object[]{context.getString(R.string.film_contrast), Integer.valueOf(indexInStack)});
            case 5:
                return String.format("%s %d", new Object[]{context.getString(R.string.film_vintage), Integer.valueOf(indexInStack)});
            default:
                return "";
        }
    }

    public static int mapFilterStyleToStackNumber(int style) {
        int stack = 0;
        for (int i = 0; i < getStackCount(); i++) {
            style -= getPresetCountForStack(i);
            if (style < 0) {
                break;
            }
            stack++;
        }
        return stack;
    }

    public static int mapStackNumberToFilterStyle(int stack) {
        int style = 0;
        for (int i = 0; i < stack; i++) {
            style += getPresetCountForStack(i);
        }
        return style;
    }

    public static int getStackCount() {
        return 6;
    }

    public static int getPresetCountForStack(int presetStack) {
        switch (presetStack) {
            case 0:
            case 1:
            case 3:
            case 4:
                return 4;
            case 2:
                return 3;
            case 5:
                return 9;
            default:
                return 0;
        }
    }
}

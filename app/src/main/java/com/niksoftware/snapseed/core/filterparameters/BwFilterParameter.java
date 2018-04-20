package com.niksoftware.snapseed.core.filterparameters;

import android.content.Context;

public class BwFilterParameter extends FilterParameter {
    private static final int[] BRIGHTNESS_DFTS = new int[]{0, 0, 20, -20, -3, 0};
    private static final int[] COLOR_STYLE_TITLE_IDS = new int[]{R.string.neutral, R.string.red_filter, R.string.orange_filter, R.string.yellow_filter, R.string.green_filter};
    private static final int[] CONTRAST_DFTS = new int[]{0, 30, 25, 20, 50, 40};
    private static final int[] GRAIN_DFTS = new int[]{0, 0, 0, 0, 65, 35};

    public int getFilterType() {
        return 7;
    }

    protected int[] getAutoParams() {
        return new int[]{3, 201, 12, 0, 1, 14, 241};
    }

    public int getDefaultValue(int param) {
        return 0;
    }

    public int getMaxValue(int param) {
        switch (param) {
            case 3:
                return 5;
            case 12:
                return 200;
            case 201:
                return 360;
            case 241:
                return 4;
            default:
                return 100;
        }
    }

    public int getMinValue(int param) {
        switch (param) {
            case 0:
            case 1:
                return -100;
            default:
                return 0;
        }
    }

    public int getDefaultParameter() {
        return 0;
    }

    public synchronized boolean setParameterValueOld(int paramKey, int paramValue) {
        boolean z;
        int i = 0;
        synchronized (this) {
            if (super.setParameterValueOld(paramKey, paramValue)) {
                if (paramKey == 241) {
                    if (paramValue != 0) {
                        i = 100;
                    }
                    super.setParameterValueOld(12, i);
                    switch (paramValue) {
                        case 0:
                        case 1:
                            super.setParameterValueOld(201, 0);
                            break;
                        case 2:
                            super.setParameterValueOld(201, 30);
                            break;
                        case 3:
                            super.setParameterValueOld(201, 60);
                            break;
                        case 4:
                            super.setParameterValueOld(201, 120);
                            break;
                    }
                } else if (paramKey == 3) {
                    super.setParameterValueOld(0, getDefaultValueForCurrentStyle(0));
                    super.setParameterValueOld(1, getDefaultValueForCurrentStyle(1));
                    super.setParameterValueOld(14, getDefaultValueForCurrentStyle(14));
                    if (paramValue == 5) {
                        i = 1;
                    }
                    setParameterValueOld(241, i);
                }
                z = true;
            }
        }
        return z;
    }

    public boolean affectsPanorama() {
        return false;
    }

    private int getDefaultValueForCurrentStyle(int param) {
        switch (param) {
            case 0:
                return BRIGHTNESS_DFTS[getParameterValueOld(3)];
            case 1:
                return CONTRAST_DFTS[getParameterValueOld(3)];
            case 14:
                return GRAIN_DFTS[getParameterValueOld(3)];
            default:
                return getDefaultValue(param);
        }
    }

    public String getParameterDescription(Context context, int parameterType, Object parameterValue) {
        switch (parameterType) {
            case 3:
                return getStyleDescription(context, (Integer) parameterValue);
            case 241:
                return getColorStyleDescription(context, (Integer) parameterValue);
            default:
                return super.getParameterDescription(context, parameterType, parameterValue);
        }
    }

    private String getStyleDescription(Context context, Integer styleId) {
        int intValue;
        if (styleId != null) {
            intValue = styleId.intValue();
        } else {
            intValue = -1;
        }
        switch (intValue) {
            case 0:
                return "Neutral";
            case 1:
                return "Contrast";
            case 2:
                return "Bright";
            case 3:
                return "Dark";
            case 4:
                return "Film";
            case 5:
                return "Darken Sky";
            default:
                return "*UNKNOWN*";
        }
    }

    private String getColorStyleDescription(Context context, Integer styleId) {
        return (context == null || styleId == null || styleId.intValue() < 0 || styleId.intValue() >= COLOR_STYLE_TITLE_IDS.length) ? "*UNKNOWN*" : context.getString(COLOR_STYLE_TITLE_IDS[styleId.intValue()]);
    }
}

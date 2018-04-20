package com.niksoftware.snapseed.core.filterparameters;

import android.content.Context;

public class CenterFocusParameter extends FilterParameter {
    public int getFilterType() {
        return 11;
    }

    protected int[] getAutoParams() {
        return new int[]{12, 5, 24, 25, 4, 22, 19, 23, 3};
    }

    public int getDefaultValue(int param) {
        switch (param) {
            case 3:
            case 12:
                return 0;
            case 4:
                return 27;
            case 19:
                return 13;
            case 22:
                return 30;
            case 23:
                return -35;
            default:
                return FilterParameter.packDouble2LowerInt(0.5d);
        }
    }

    public int getMaxValue(int param) {
        switch (param) {
            case 3:
                return 5;
            case 5:
            case 24:
            case 25:
                return Integer.MAX_VALUE;
            default:
                return 100;
        }
    }

    public int getMinValue(int param) {
        switch (param) {
            case 5:
            case 24:
            case 25:
                return Integer.MIN_VALUE;
            case 22:
            case 23:
                return -100;
            default:
                return 0;
        }
    }

    public int getDefaultParameter() {
        return 19;
    }

    public String getParameterDescription(Context context, int parameterType, Object parameterValue) {
        if (parameterType != 3) {
            return super.getParameterDescription(context, parameterType, parameterValue);
        }
        switch (((Integer) parameterValue).intValue()) {
            case 0:
                return "Portrait 1";
            case 1:
                return "Portrait 2";
            case 2:
                return "Vignette";
            case 3:
                return "Blur";
            case 4:
                return "Old Lens";
            case 5:
                return "Foggy";
            default:
                return "*UNKNOWN*";
        }
    }

    private void preset(int filterStrength, int blurStrength, int vignetteStrength, int brightness, float centerSize) {
        setParameterValueOld(12, filterStrength);
        setParameterValueOld(19, blurStrength);
        setParameterValueOld(23, vignetteStrength);
        setParameterValueOld(22, brightness);
        setParameterValueOld(4, Math.round(100.0f * centerSize));
    }

    public synchronized boolean setParameterValueOld(int paramKey, int paramValue) {
        boolean z = false;
        synchronized (this) {
            if (super.setParameterValueOld(paramKey, paramValue)) {
                if (paramKey == 3) {
                    switch (paramValue) {
                        case 0:
                            preset(0, 13, -35, 30, 0.27f);
                            break;
                        case 1:
                            preset(0, 0, -83, 25, 0.33f);
                            break;
                        case 2:
                            preset(0, 0, -95, 0, 0.55f);
                            break;
                        case 3:
                            preset(1, 66, -50, 0, 0.38f);
                            break;
                        case 4:
                            preset(1, 40, -85, 0, 0.33f);
                            break;
                        case 5:
                            preset(0, 85, 16, 20, 0.23f);
                            break;
                    }
                }
                z = true;
            }
        }
        return z;
    }
}

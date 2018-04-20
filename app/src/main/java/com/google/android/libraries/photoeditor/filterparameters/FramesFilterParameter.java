package com.google.android.libraries.photoeditor.filterparameters;

import android.content.Context;

public class FramesFilterParameter extends FilterParameter {
    public int getFilterType() {
        return 12;
    }

    protected int[] getAutoParams() {
        return new int[]{102, 201, 202, 203, 204, 103, 113, 211, 212, 222, 221, 3};
    }

    public int getDefaultValue(int param) {
        switch (param) {
            case 3:
                return -1;
            case 102:
                return 1000;
            case 103:
            case 113:
                return 0;
            case 201:
            case 212:
                return 1;
            case 202:
                return 10;
            case 203:
                return 10;
            case 204:
                return 0;
            case 211:
                return 0;
            case 221:
                return 85;
            case 222:
                return 15;
            default:
                return 0;
        }
    }

    public int getMaxValue(int param) {
        switch (param) {
            case 3:
                return 7;
            case 102:
                return 1000;
            case 103:
            case 113:
                return Integer.MAX_VALUE;
            default:
                return 100;
        }
    }

    public int getMinValue(int param) {
        return 0;
    }

    public int getDefaultParameter() {
        return 222;
    }

    public int getParameterValueOld(int paramKey) {
        if (paramKey == 223) {
            paramKey = 3;
        }
        return super.getParameterValueOld(paramKey);
    }

    public synchronized boolean setParameterValueOld(int paramKey, int paramValue) {
        boolean parameterValueOld;
        if (paramKey == 223) {
            paramKey = 3;
        }
        if (paramKey != 3) {
            parameterValueOld = super.setParameterValueOld(paramKey, paramValue);
        } else if (paramValue == -1) {
            int old = ((Integer) this.parameterValues.get(3)).intValue();
            this.parameterValues.put(3, Integer.valueOf(paramValue));
            parameterValueOld = old != -1;
        } else if (super.setParameterValueOld(paramKey, paramValue)) {
            super.setParameterValueOld(202, defaultValueForCurrentStyle(getParameterValueOld(3), 202));
            super.setParameterValueOld(203, defaultValueForCurrentStyle(getParameterValueOld(3), 203));
            super.setParameterValueOld(204, defaultValueForCurrentStyle(getParameterValueOld(3), 204));
            parameterValueOld = true;
        } else {
            parameterValueOld = false;
        }
        return parameterValueOld;
    }

    int defaultValueForCurrentStyle(int style, int param) {
        if (style > 7) {
            return getDefaultValue(param);
        }
        switch (param) {
            case 202:
                switch (style) {
                    case 0:
                    case 1:
                    case 4:
                    case 5:
                        return 2;
                    case 2:
                        return 13;
                    case 3:
                        return 13;
                    case 6:
                        return 13;
                    case 7:
                        return 13;
                    default:
                        break;
                }
            case 203:
                switch (style) {
                    case 0:
                    case 1:
                    case 2:
                    case 3:
                        return 2;
                    case 4:
                        return 13;
                    case 5:
                        return 13;
                    case 6:
                        return 13;
                    case 7:
                        return 13;
                    default:
                        break;
                }
            case 204:
                if (style >= 0 && style <= 7) {
                    return style % 2;
                }
        }
        return getDefaultValue(param);
    }

    public String getParameterDescription(Context context, int parameterType, Object parameterValue) {
        throw new UnsupportedOperationException("Not implemented");
    }
}

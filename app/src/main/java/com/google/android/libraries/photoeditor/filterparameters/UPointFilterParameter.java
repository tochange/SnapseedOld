package com.google.android.libraries.photoeditor.filterparameters;

import android.content.Context;
import java.util.ArrayList;

public class UPointFilterParameter extends FilterParameter {
    protected void init(int[] parameterKeys) {
        super.init(parameterKeys);
        this.subParameters = new ArrayList();
    }

    public int getFilterType() {
        return 3;
    }

    protected int[] getAutoParams() {
        return new int[0];
    }

    public int getDefaultValue(int param) {
        switch (param) {
        }
        return 0;
    }

    public int getMaxValue(int param) {
        switch (param) {
            case 0:
            case 1:
            case 2:
                return 100;
            default:
                return 0;
        }
    }

    public int getMinValue(int param) {
        switch (param) {
            case 0:
            case 1:
            case 2:
                return -100;
            default:
                return 0;
        }
    }

    public int getDefaultParameter() {
        return 1000;
    }

    public String getParameterDescription(Context context, int parameterType, Object parameterValue) {
        String description = null;
        if (parameterType >= 0) {
            UPointParameter upoint = getUPoint(parameterType);
            if (upoint != null) {
                description = upoint.getParameterDescription(context, upoint.getActiveFilterParameter());
            }
        }
        return description != null ? description : super.getParameterDescription(context, parameterType, parameterValue);
    }

    public UPointParameter addUPoint(int x, int y) {
        UPointParameter upoint = new UPointParameter();
        addSubParameters(upoint);
        activateUPoint(upoint);
        return upoint;
    }

    public void deleteUPoint(UPointParameter param) {
        removeSubParameters(param);
        activateUPoint(null);
    }

    public void activateUPoint(UPointParameter param) {
        this.activeFilterParameter = -1;
        for (int i = 0; i < this.subParameters.size(); i++) {
            FilterParameter parameter = (FilterParameter) this.subParameters.get(i);
            if (parameter instanceof UPointParameter) {
                UPointParameter upoint = (UPointParameter) parameter;
                if (upoint == param) {
                    upoint.setSelected(true);
                    this.activeFilterParameter = i;
                } else {
                    upoint.setSelected(false);
                }
            }
        }
    }

    public int getUPointCount() {
        return this.subParameters.size();
    }

    public UPointParameter getUPoint(int index) {
        if (index < 0 || index >= this.subParameters.size()) {
            return null;
        }
        FilterParameter parameter = (FilterParameter) this.subParameters.get(index);
        if (parameter instanceof UPointParameter) {
            return (UPointParameter) parameter;
        }
        throw new IllegalStateException("Invalid UPoint sub-parameter type");
    }

    public UPointParameter getActiveUPoint() {
        return getUPoint(this.activeFilterParameter);
    }
}

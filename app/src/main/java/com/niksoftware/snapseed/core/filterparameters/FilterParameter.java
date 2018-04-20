package com.niksoftware.snapseed.core.filterparameters;

import android.content.Context;
import android.util.SparseArray;

import com.niksoftware.snapseed.R;
import com.niksoftware.snapseed.core.FilterDefs.FilterParameterType;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public abstract class FilterParameter implements Cloneable {
    protected static final String INVALID_DESCRIPTION = "*UNKNOWN*";
    protected int activeFilterParameter;
    protected final List<FilterParameterListener> listeners = new ArrayList();
    protected int[] parameterKeys;
    protected SparseArray parameterValues;
    protected ArrayList<FilterParameter> subParameters;

    public interface FilterParameterListener {
        void onActiveParameterChanged(int i);

        void onParameterValueChanged(int i, Object obj);
    }

    protected abstract int[] getAutoParams();

    public abstract int getDefaultParameter();

    public abstract int getDefaultValue(int i);

    public abstract int getFilterType();

    public abstract int getMaxValue(int i);

    public abstract int getMinValue(int i);

    public FilterParameter() {
        init(getAutoParams());
    }

    protected void init(int[] parameterKeys) {
        this.parameterKeys = parameterKeys;
        this.parameterValues = new SparseArray(this.parameterKeys.length);
        for (int key : this.parameterKeys) {
            this.parameterValues.put(key, Integer.valueOf(getDefaultValue(key)));
        }
        this.activeFilterParameter = getDefaultParameter();
    }

    public synchronized int getParameterValueOld(int parameterKey) {
        return ((Integer) this.parameterValues.get(parameterKey, Integer.valueOf(0))).intValue();
    }

    public synchronized boolean setParameterValueOld(int paramKey, int paramValue) {
        boolean z;
        Integer oldValue = (Integer) this.parameterValues.get(paramKey);
        if (oldValue == null || oldValue.equals(Integer.valueOf(paramValue))) {
            z = false;
        } else {
            paramValue = Math.min(getMaxValue(paramKey), Math.max(getMinValue(paramKey), paramValue));
            this.parameterValues.put(paramKey, Integer.valueOf(paramValue));
            onParameterValueChanged(paramKey, Integer.valueOf(paramValue));
            z = true;
        }
        return z;
    }

    public synchronized Object getParameterValue(int parameterKey) {
        return Float.valueOf((float) ((Integer) this.parameterValues.get(parameterKey)).intValue());
    }

    public synchronized boolean setParameterFloat(int parameterKey, float value) {
        return setParameterValueOld(parameterKey, (int) value);
    }

    public synchronized boolean setParameterPoint(int parameterKey, float x, float y) {
        return false;
    }

    public boolean affectsPanorama() {
        return true;
    }

    public static double lowBitsAsDouble(int value) {
        return ((double) (65535 & value)) / 65535.0d;
    }

    public static int packDouble2LowerInt(double x) {
        return (int) (65535.0d * Math.min(Math.max(x, 0.0d), 1.0d));
    }

    public synchronized FilterParameter clone() throws CloneNotSupportedException {
        FilterParameter copy;
        copy = (FilterParameter) super.clone();
        copy.parameterKeys = (int[]) this.parameterKeys.clone();
        copy.parameterValues = this.parameterValues.clone();
        copy.activeFilterParameter = this.activeFilterParameter;
        if (this.subParameters != null) {
            copy.subParameters = new ArrayList(this.subParameters.size());
            Iterator i$ = this.subParameters.iterator();
            while (i$.hasNext()) {
                copy.addSubParameters(((FilterParameter) i$.next()).clone());
            }
        }
        return copy;
    }

    public synchronized int getActiveFilterParameter() {
        return this.activeFilterParameter;
    }

    public synchronized void setActiveFilterParameter(int parameterKey) {
        if (this.activeFilterParameter != parameterKey) {
            this.activeFilterParameter = parameterKey;
            onActiveParameterChanged(this.activeFilterParameter);
        }
    }

    public int[] getParameterKeys() {
        return this.parameterKeys;
    }

    public ArrayList<FilterParameter> getSubParameters() {
        return this.subParameters;
    }

    public void addSubParameters(FilterParameter param) {
        if (this.subParameters != null) {
            for (FilterParameterListener listener : this.listeners) {
                param.addListener(listener);
            }
            this.subParameters.add(param);
        }
    }

    public boolean removeSubParameters(FilterParameter param) {
        if (this.subParameters == null || !this.subParameters.contains(param)) {
            return false;
        }
        this.subParameters.remove(param);
        return true;
    }

    public boolean isDefaultParameter(int parameterType) {
        return getDefaultParameter() == parameterType;
    }

    public int[] getParameterValues(int parameterType) {
        int min = getMinValue(parameterType);
        int max = getMaxValue(parameterType);
        int[] values = new int[((max - min) + 1)];
        for (int i = min; i <= max; i++) {
            values[i - min] = i;
        }
        return values;
    }

    public int getRandomValue(int param) {
        int minValue = getMinValue(param);
        return new Random().nextInt((getMaxValue(param) - minValue) + 1) + minValue;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getRandomValueNoMatch(int r8, int r9) {
        /*
        r7 = this;
        r0 = 16;
        r3 = new java.util.Random;
        r3.<init>();
        r2 = r7.getMinValue(r8);
        r6 = r7.getMaxValue(r8);
        r5 = r6 - r2;
        r4 = r2;
        if (r5 <= 0) goto L_0x0023;
    L_0x0014:
        r6 = r5 + 1;
        r6 = r3.nextInt(r6);
        r4 = r2 + r6;
        if (r4 != r9) goto L_0x0023;
    L_0x001e:
        r1 = r0 + -1;
        if (r0 > 0) goto L_0x0024;
    L_0x0022:
        r0 = r1;
    L_0x0023:
        return r4;
    L_0x0024:
        r0 = r1;
        goto L_0x0014;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.niksoftware.snapseed.core.filterparameters.FilterParameter.getRandomValueNoMatch(int, int):int");
    }

    public String getParameterTitle(Context context, int parameterType) {
        return FilterParameterType.getParameterTitle(context, parameterType);
    }

    public String getParameterDescription(Context context, int parameterType, Object parameterValue) {
        switch (parameterType) {
            case 0:
            case 1:
            case 2:
            case 4:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 14:
            case 15:
            case 16:
            case 19:
            case 20:
            case 22:
            case 23:
            case 104:
            case 232:
            case 233:
                return String.format("%s %+d", new Object[]{getParameterTitle(context, parameterType), (Integer) parameterValue});
            case 3:
                return String.format("%s %d", new Object[]{context.getString(R.string.style), Integer.valueOf(((Integer) parameterValue).intValue() + 1)});
            case 5:
            case 24:
            case 25:
                return "";
            case 12:
                return context.getString(((Integer) parameterValue).intValue() > 0 ? R.string.strong : R.string.weak);
            case 101:
                return String.format("%s %d", new Object[]{context.getString(R.string.texture_label), Integer.valueOf(((Integer) parameterValue).intValue() + 1)});
            case 241:
                return getColorStyleDescription(context, null);
            default:
                return INVALID_DESCRIPTION;
        }
    }

    public String getParameterDescription(Context context, int parameterType) {
        return getParameterDescription(context, parameterType, Integer.valueOf(getParameterValueOld(parameterType)));
    }

    private String getColorStyleDescription(Context context, Integer styleId) {
        int intValue;
        if (styleId != null) {
            intValue = styleId.intValue();
        } else {
            intValue = -1;
        }
        switch (intValue) {
            case 0:
                return context.getString(R.string.neutral);
            case 1:
                return context.getString(R.string.red_filter);
            case 2:
                return context.getString(R.string.orange_filter);
            case 3:
                return context.getString(R.string.yellow_filter);
            case 4:
                return context.getString(R.string.green_filter);
            default:
                return INVALID_DESCRIPTION;
        }
    }

    public void addListener(FilterParameterListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener cannot be null");
        } else if (!this.listeners.contains(listener)) {
            this.listeners.add(listener);
            if (this.subParameters != null) {
                Iterator i$ = this.subParameters.iterator();
                while (i$.hasNext()) {
                    ((FilterParameter) i$.next()).addListener(listener);
                }
            }
        }
    }

    public void onActiveParameterChanged(int parameterType) {
        for (FilterParameterListener listener : this.listeners) {
            listener.onActiveParameterChanged(parameterType);
        }
    }

    public void onParameterValueChanged(int parameterType, Object value) {
        for (FilterParameterListener listener : this.listeners) {
            listener.onParameterValueChanged(parameterType, value);
        }
    }
}

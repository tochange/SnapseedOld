package com.niksoftware.snapseed.core;

import android.content.Context;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;

public class UndoObject {
    private int _changedParameter;
    private String _description;
    private boolean _forceNoDescription;
    private FilterParameter _parameter;

    private UndoObject(FilterParameter param, int changedParameter, String description, boolean forceNoDescription) {
        try {
            this._parameter = param.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        this._changedParameter = changedParameter;
        this._description = description;
        this._forceNoDescription = forceNoDescription;
    }

    public UndoObject(FilterParameter param, int changedParameter, String description) {
        this(param, changedParameter, description, false);
    }

    public UndoObject(FilterParameter param, int changedParameter, boolean forceNoDescription) {
        this(param, changedParameter, null, forceNoDescription);
    }

    public FilterParameter getFilterParameter() {
        return this._parameter;
    }

    public int getChangedParameter() {
        return this._changedParameter;
    }

    public boolean hasStaticDescription() {
        return this._description != null;
    }

    public String getDescription(Context context) {
        if (this._forceNoDescription) {
            return null;
        }
        return this._description != null ? this._description : this._parameter.getParameterDescription(context, this._changedParameter);
    }

    public boolean forceNoDescription() {
        return this._forceNoDescription;
    }
}

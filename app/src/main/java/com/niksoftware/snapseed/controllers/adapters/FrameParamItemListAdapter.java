package com.niksoftware.snapseed.controllers.adapters;

import android.content.Context;

import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.views.ItemSelectorView.ListAdapter;

public class FrameParamItemListAdapter implements ListAdapter {
    static final /* synthetic */ boolean $assertionsDisabled = (!FrameParamItemListAdapter.class.desiredAssertionStatus());
    private FilterParameter _filterParameter;

    public FrameParamItemListAdapter(FilterParameter filterParameter) {
        if ($assertionsDisabled || filterParameter != null) {
            this._filterParameter = filterParameter;
            return;
        }
        throw new AssertionError("Filter parameter should not be null");
    }

    public void updateFilterParameter(FilterParameter filterParameter) {
        this._filterParameter = filterParameter;
    }

    public int getItemCount() {
        return 2;
    }

    public Integer getItemId(int itemIndex) {
        return Integer.valueOf(itemIndex);
    }

    public Object[] getItemStateImages(Context context, Integer itemId) {
        switch (itemId.intValue()) {
            case 0:
                return new Integer[]{Integer.valueOf(R.drawable.icon_fo_square_default), Integer.valueOf(R.drawable.icon_fo_square_active)};
            case 1:
                return new Integer[]{Integer.valueOf(R.drawable.icon_fo_tools_default), Integer.valueOf(R.drawable.icon_fo_tools_active)};
            default:
                return null;
        }
    }

    public int getItemParameterType(Integer itemId) {
        switch (itemId.intValue()) {
            case 0:
                return 224;
            case 1:
                return 9;
            default:
                if ($assertionsDisabled) {
                    return 1000;
                }
                throw new AssertionError("Invalid item id");
        }
    }

    public String getItemText(Context context, Integer itemId) {
        switch (itemId.intValue()) {
            case 0:
                return context.getString(R.string.format);
            case 1:
                return context.getString(R.string.colorized);
            default:
                if ($assertionsDisabled) {
                    return null;
                }
                throw new AssertionError("Invalid item id");
        }
    }

    public boolean isItemActive(Integer itemId) {
        switch (itemId.intValue()) {
            case 0:
                if (this._filterParameter.getParameterValueOld(224) == 1) {
                    return true;
                }
                return false;
            case 1:
                boolean isActive;
                if (this._filterParameter.getParameterValueOld(9) != 0) {
                    isActive = true;
                } else {
                    isActive = false;
                }
                return isActive;
            default:
                if ($assertionsDisabled) {
                    return false;
                }
                throw new AssertionError("Invalid item id");
        }
    }

    public boolean hasContextItem() {
        return false;
    }

    public int getContextButtonImageId() {
        return 0;
    }

    public String getContextButtonText(Context context) {
        return null;
    }
}

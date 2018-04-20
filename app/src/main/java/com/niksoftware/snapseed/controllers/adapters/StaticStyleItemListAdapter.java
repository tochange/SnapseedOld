package com.niksoftware.snapseed.controllers.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.views.ItemSelectorView.ListAdapter;

public class StaticStyleItemListAdapter implements ListAdapter {
    static final /* synthetic */ boolean $assertionsDisabled = (!StaticStyleItemListAdapter.class.desiredAssertionStatus());
    private Integer _activeItemId;
    private int _contextButtonImageId;
    private int _contextButtonTitleId;
    private FilterParameter _filterParameter;
    private int[] _itemIds;
    private int _parameterId;
    private Bitmap[] _stylePreviewImages;
    private int[] _stylePreviewResIds;

    public StaticStyleItemListAdapter(FilterParameter filterParameter, int styleParamId, int[] stylePreviewResIds, int[] itemIds) {
        if (!$assertionsDisabled && (stylePreviewResIds == null || (filterParameter.getMaxValue(styleParamId) - filterParameter.getMinValue(styleParamId)) + 1 != stylePreviewResIds.length / 2)) {
            throw new AssertionError("Invalid preview drawable set");
        } else if ($assertionsDisabled || itemIds == null || itemIds.length == stylePreviewResIds.length / 2) {
            this._filterParameter = filterParameter;
            this._parameterId = styleParamId;
            this._stylePreviewResIds = stylePreviewResIds;
            this._itemIds = itemIds;
        } else {
            throw new AssertionError("Item id count doesn't match preview styles count");
        }
    }

    public StaticStyleItemListAdapter(FilterParameter filterParameter, int styleParamId, int[] stylePreviewResIds) {
        this(filterParameter, styleParamId, stylePreviewResIds, null);
    }

    public StaticStyleItemListAdapter(FilterParameter filterParameter, int styleParamId, Bitmap[] stylePreviews, int[] itemIds) {
        if (!$assertionsDisabled && (stylePreviews == null || (filterParameter.getMaxValue(styleParamId) - filterParameter.getMinValue(styleParamId)) + 1 != stylePreviews.length / 2)) {
            throw new AssertionError("Invalid preview drawable set");
        } else if ($assertionsDisabled || itemIds == null || itemIds.length == stylePreviews.length / 2) {
            this._filterParameter = filterParameter;
            this._parameterId = styleParamId;
            this._stylePreviewImages = stylePreviews;
            this._itemIds = itemIds;
        } else {
            throw new AssertionError("Item id count doesn't match preview styles count");
        }
    }

    public StaticStyleItemListAdapter(FilterParameter filterParameter, int styleParamId, Bitmap[] stylePreviewResId) {
        this(filterParameter, styleParamId, stylePreviewResId, null);
    }

    public void setContextButtonAppearance(int imageId, int titleId) {
        this._contextButtonImageId = imageId;
        this._contextButtonTitleId = titleId;
    }

    public void setActiveItemId(Integer activeItemId) {
        this._activeItemId = activeItemId;
    }

    public Integer getActiveItemId() {
        return this._activeItemId;
    }

    public int getItemCount() {
        return this._stylePreviewResIds != null ? this._stylePreviewResIds.length / 2 : this._stylePreviewImages.length / 2;
    }

    public Integer getItemId(int itemIndex) {
        if (this._itemIds != null) {
            return (itemIndex < 0 || itemIndex >= this._itemIds.length) ? null : Integer.valueOf(this._itemIds[itemIndex]);
        } else {
            return Integer.valueOf(itemIndex);
        }
    }

    private int getItemIndex(Integer itemId) {
        if (this._itemIds != null) {
            for (int i = 0; i < this._itemIds.length; i++) {
                if (this._itemIds[i] == itemId.intValue()) {
                    return i;
                }
            }
            return -1;
        } else if (itemId == null || itemId.intValue() < 0 || itemId.intValue() >= getItemCount()) {
            return -1;
        } else {
            return itemId.intValue();
        }
    }

    public Object[] getItemStateImages(Context context, Integer itemId) {
        if (getItemIndex(itemId) < 0) {
            return null;
        }
        if (this._stylePreviewResIds != null) {
            return new Integer[]{Integer.valueOf(this._stylePreviewResIds[itemIndex * 2]), Integer.valueOf(this._stylePreviewResIds[(itemIndex * 2) + 1])};
        }
        return new Bitmap[]{this._stylePreviewImages[itemIndex * 2], this._stylePreviewImages[(itemIndex * 2) + 1]};
    }

    public String getItemText(Context context, Integer itemId) {
        return this._filterParameter.getParameterDescription(context, this._parameterId, itemId);
    }

    public boolean isItemActive(Integer itemId) {
        return itemId != null && itemId.equals(this._activeItemId);
    }

    public boolean hasContextItem() {
        return this._contextButtonImageId > 0 && this._contextButtonTitleId > 0;
    }

    public int getContextButtonImageId() {
        return this._contextButtonImageId;
    }

    public String getContextButtonText(Context context) {
        return context.getString(this._contextButtonTitleId);
    }
}

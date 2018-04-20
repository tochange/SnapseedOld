package com.niksoftware.snapseed.controllers.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.views.ItemSelectorView.ListAdapter;

public class ColorStyleItemListAdapter implements ListAdapter {
    private static final int[] COLOR_STYLE_BITMAP_IDS = new int[]{R.drawable.icon_fo_lens_neutral_default, R.drawable.icon_fo_lens_neutral_active, R.drawable.icon_fo_lens_red_default, R.drawable.icon_fo_lens_red_active, R.drawable.icon_fo_lens_orange_default, R.drawable.icon_fo_lens_orange_active, R.drawable.icon_fo_lens_yellow_default, R.drawable.icon_fo_lens_yellow_active, R.drawable.icon_fo_lens_green_default, R.drawable.icon_fo_lens_green_active};
    private static final int COLOR_STYLE_COUNT = 5;
    private Integer _activeItemId = Integer.valueOf(0);
    private FilterParameter _filterParameter;

    public ColorStyleItemListAdapter(FilterParameter filterParameter) {
        this._filterParameter = filterParameter;
    }

    public void setActiveItemId(Integer activeItemId) {
        this._activeItemId = activeItemId;
    }

    public int getItemCount() {
        return 5;
    }

    public Integer getItemId(int itemIndex) {
        return Integer.valueOf(itemIndex);
    }

    public Bitmap[] getItemStateImages(Context context, Integer itemId) {
        if (itemId == null || itemId.intValue() < 0 || itemId.intValue() >= 5) {
            return null;
        }
        Resources resources = context.getResources();
        return new Bitmap[]{BitmapFactory.decodeResource(resources, COLOR_STYLE_BITMAP_IDS[itemId.intValue() * 2]), BitmapFactory.decodeResource(resources, COLOR_STYLE_BITMAP_IDS[(itemId.intValue() * 2) + 1])};
    }

    public String getItemText(Context context, Integer itemId) {
        return this._filterParameter.getParameterDescription(context, 241, itemId);
    }

    public boolean isItemActive(Integer itemId) {
        return itemId != null && itemId.equals(this._activeItemId);
    }

    public boolean hasContextItem() {
        return false;
    }

    public int getContextButtonImageId() {
        return -1;
    }

    public String getContextButtonText(Context context) {
        return null;
    }
}

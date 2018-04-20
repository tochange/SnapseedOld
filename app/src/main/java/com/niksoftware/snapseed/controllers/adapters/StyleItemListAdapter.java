package com.niksoftware.snapseed.controllers.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.util.BitmapHelper;
import com.niksoftware.snapseed.views.ItemSelectorView.ListAdapter;
import java.util.ArrayList;
import java.util.List;

public class StyleItemListAdapter implements ListAdapter {
    static final /* synthetic */ boolean $assertionsDisabled = (!StyleItemListAdapter.class.desiredAssertionStatus());
    private int _activeBorderColor;
    private int _activeBorderSize;
    private Integer _activeItemId;
    private final ActiveItemOverlayProvider _activeOverlayProvider;
    private int _contextButtonImageId;
    private int _contextButtonTitleId;
    private FilterParameter _filterParameter;
    private int _parameterId;
    private int _previewEdgeSpace;
    private int _previewSize;
    private List<Bitmap> _stylePreviews;

    public interface ActiveItemOverlayProvider {
        Drawable getActiveItemOverlayDrawable(Integer num);
    }

    public StyleItemListAdapter(Context context, FilterParameter filterParameter, int styleParamId, Bitmap stylePreviewSource) {
        init(context, filterParameter, styleParamId, stylePreviewSource);
        this._activeOverlayProvider = null;
    }

    public StyleItemListAdapter(Context context, FilterParameter filterParameter, int styleParamId, ActiveItemOverlayProvider activeOverlayProvider, Bitmap stylePreviewSource) {
        if ($assertionsDisabled || activeOverlayProvider != null) {
            init(context, filterParameter, styleParamId, stylePreviewSource);
            this._activeOverlayProvider = activeOverlayProvider;
            return;
        }
        throw new AssertionError("Invalid input parameter");
    }

    private void init(Context context, FilterParameter filterParameter, int styleParamId, Bitmap stylePreviewSource) {
        this._filterParameter = filterParameter;
        this._parameterId = styleParamId;
        Resources resources = context.getResources();
        this._previewSize = resources.getDimensionPixelSize(R.dimen.tb_subpanel_preview_size);
        this._previewEdgeSpace = resources.getDimensionPixelSize(R.dimen.tb_subpanel_preview_edge_space);
        this._activeBorderSize = resources.getDimensionPixelSize(R.dimen.tb_subpanel_preview_border_size);
        this._activeBorderColor = resources.getColor(R.color.tb_subpanel_selected_item_title);
        Bitmap defaultPreview = BitmapHelper.cropAndBorderBitmap(stylePreviewSource, this._previewSize, this._previewSize, this._previewEdgeSpace, 0, 0);
        int styleCount = getItemCountForPreviewRendering(filterParameter, styleParamId);
        this._stylePreviews = new ArrayList(styleCount);
        int styleCount2 = styleCount;
        while (true) {
            styleCount = styleCount2 - 1;
            if (styleCount2 > 0) {
                this._stylePreviews.add(defaultPreview);
                styleCount2 = styleCount;
            } else {
                return;
            }
        }
    }

    public void updateFilterParameter(FilterParameter filterParameter) {
        this._filterParameter = filterParameter;
    }

    public void setContextButtonAppearance(int imageId, int titleId) {
        this._contextButtonImageId = imageId;
        this._contextButtonTitleId = titleId;
    }

    public boolean updateStylePreviews(List<Bitmap> stylePreviews) {
        if (stylePreviews == null) {
            if (this._stylePreviews == null) {
                return true;
            }
            return false;
        } else if (this._stylePreviews == null || this._stylePreviews.size() != stylePreviews.size()) {
            return false;
        } else {
            this._stylePreviews = stylePreviews;
            return true;
        }
    }

    public void setActiveItemId(Integer activeItemId) {
        this._activeItemId = activeItemId;
    }

    public Integer getActiveItemId() {
        return this._activeItemId;
    }

    public int getItemCount() {
        return this._stylePreviews.size();
    }

    public Integer getItemId(int itemIndex) {
        return Integer.valueOf(itemIndex);
    }

    public Object[] getItemStateImages(Context context, Integer itemId) {
        if (itemId == null || itemId.intValue() < 0 || itemId.intValue() >= this._stylePreviews.size()) {
            return null;
        }
        Bitmap stylePreview = (Bitmap) this._stylePreviews.get(itemId.intValue());
        if (this._activeOverlayProvider == null) {
            return new Bitmap[]{BitmapHelper.cropAndBorderBitmap(stylePreview, this._previewSize, this._previewSize, this._previewEdgeSpace, 0, 0), BitmapHelper.cropAndBorderBitmap(stylePreview, this._previewSize, this._previewSize, this._previewEdgeSpace, this._activeBorderSize, this._activeBorderColor)};
        }
        stylePreview = BitmapHelper.cropAndBorderBitmap(stylePreview, this._previewSize, this._previewSize, this._previewEdgeSpace, 0, 0);
        return new Bitmap[]{stylePreview, BitmapHelper.composeBitmaps(stylePreview, this._activeOverlayProvider.getActiveItemOverlayDrawable(itemId))};
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

    protected int getItemCountForPreviewRendering(FilterParameter param, int paramId) {
        return (param.getMaxValue(paramId) - param.getMinValue(paramId)) + 1;
    }
}

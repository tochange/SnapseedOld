package com.niksoftware.snapseed.controllers;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.niksoftware.snapseed.controllers.adapters.ColorStyleItemListAdapter;
import com.niksoftware.snapseed.controllers.adapters.StyleItemListAdapter;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.BaseFilterButton;
import com.niksoftware.snapseed.views.ItemSelectorView;
import com.niksoftware.snapseed.views.ItemSelectorView.OnClickListener;
import com.niksoftware.snapseed.views.ItemSelectorView.OnVisibilityChangeListener;
import java.util.List;

public class BlackAndWhiteController extends EmptyFilterController {
    private OnClickListener _colorFilterClickListener;
    private ColorStyleItemListAdapter _colorFilterListAdapter;
    private BaseFilterButton _colorFilterStyleButton;
    private ItemSelectorView _itemSelectorView;
    private BaseFilterButton _presetButton;
    private OnClickListener _presetClickListener;
    private StyleItemListAdapter _presetListAdapter;

    public void init(ControllerContext controllerContext) {
        super.init(controllerContext);
        addParameterHandler();
        Context context = getContext();
        FilterParameter filter = getFilterParameter();
        this._colorFilterListAdapter = new ColorStyleItemListAdapter(filter);
        this._colorFilterClickListener = new OnClickListener() {
            public boolean onItemClick(Integer itemId) {
                if (BlackAndWhiteController.this.changeParameter(BlackAndWhiteController.this.getFilterParameter(), 241, itemId.intValue())) {
                    TrackerData.getInstance().usingParameter(241, false);
                    BlackAndWhiteController.this._colorFilterListAdapter.setActiveItemId(itemId);
                    BlackAndWhiteController.this._itemSelectorView.refreshSelectorItems(BlackAndWhiteController.this._colorFilterListAdapter, true);
                }
                return true;
            }

            public boolean onContextButtonClick() {
                return false;
            }
        };
        this._presetListAdapter = new StyleItemListAdapter(context, filter, 3, getTilesProvider().getStyleSourceImage());
        this._presetClickListener = new OnClickListener() {
            public boolean onItemClick(Integer itemId) {
                if (BlackAndWhiteController.this.changeParameter(BlackAndWhiteController.this.getFilterParameter(), 3, itemId.intValue())) {
                    TrackerData.getInstance().usingParameter(3, false);
                    BlackAndWhiteController.this._presetListAdapter.setActiveItemId(itemId);
                    BlackAndWhiteController.this._itemSelectorView.refreshSelectorItems(BlackAndWhiteController.this._presetListAdapter, true);
                }
                return true;
            }

            public boolean onContextButtonClick() {
                return false;
            }
        };
        this._itemSelectorView = getItemSelectorView();
        this._itemSelectorView.setOnVisibilityChangeListener(new OnVisibilityChangeListener() {
            public void onVisibilityChanged(boolean isVisible) {
                if (!isVisible) {
                    BlackAndWhiteController.this._presetButton.setSelected(false);
                    BlackAndWhiteController.this._colorFilterStyleButton.setSelected(false);
                }
            }
        });
    }

    public void cleanup() {
        getEditingToolbar().itemSelectorWillHide();
        this._itemSelectorView.setVisible(false, false);
        this._itemSelectorView.cleanup();
        this._itemSelectorView = null;
        super.cleanup();
    }

    public int getFilterType() {
        return 7;
    }

    public int[] getGlobalAdjustmentParameters() {
        return new int[]{0, 1, 14};
    }

    public boolean showsParameterView() {
        return true;
    }

    public boolean initLeftFilterButton(BaseFilterButton button) {
        if (button == null) {
            this._presetButton = null;
            return false;
        }
        this._presetButton = button;
        this._presetButton.setStateImages((int) R.drawable.icon_tb_presets_default, (int) R.drawable.icon_tb_presets_active, 0);
        this._presetButton.setText(getButtonTitle(R.string.presets));
        this._presetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                BlackAndWhiteController.this._presetButton.setSelected(true);
                BlackAndWhiteController.this._presetListAdapter.setActiveItemId(Integer.valueOf(BlackAndWhiteController.this.getFilterParameter().getParameterValueOld(3)));
                BlackAndWhiteController.this._itemSelectorView.reloadSelector(BlackAndWhiteController.this._presetListAdapter, BlackAndWhiteController.this._presetClickListener);
                BlackAndWhiteController.this._itemSelectorView.setVisible(true, true);
                BlackAndWhiteController.this.previewImages(3);
            }
        });
        return true;
    }

    public boolean initRightFilterButton(BaseFilterButton button) {
        if (button == null) {
            this._colorFilterStyleButton = null;
            return false;
        }
        this._colorFilterStyleButton = button;
        this._colorFilterStyleButton.setStateImages((int) R.drawable.icon_tb_colorfilter_default, (int) R.drawable.icon_tb_colorfilter_active, 0);
        this._colorFilterStyleButton.setText(getButtonTitle(R.string.colorFilter));
        this._colorFilterStyleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                BlackAndWhiteController.this._colorFilterStyleButton.setSelected(true);
                BlackAndWhiteController.this._colorFilterListAdapter.setActiveItemId(Integer.valueOf(BlackAndWhiteController.this.getFilterParameter().getParameterValueOld(241)));
                BlackAndWhiteController.this._itemSelectorView.reloadSelector(BlackAndWhiteController.this._colorFilterListAdapter, BlackAndWhiteController.this._colorFilterClickListener);
                BlackAndWhiteController.this._itemSelectorView.setVisible(true, true);
            }
        });
        return true;
    }

    public BaseFilterButton getLeftFilterButton() {
        return this._presetButton;
    }

    public BaseFilterButton getRightFilterButton() {
        return this._colorFilterStyleButton;
    }

    public void setPreviewImage(List<Bitmap> images, int parameter) {
        if (this._presetListAdapter.updateStylePreviews(images)) {
            this._itemSelectorView.refreshSelectorItems(this._presetListAdapter, false);
        }
    }
}

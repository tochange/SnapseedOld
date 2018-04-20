package com.niksoftware.snapseed.controllers;

import android.view.View;

import com.niksoftware.snapseed.controllers.adapters.StaticStyleItemListAdapter;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.BaseFilterButton;
import com.niksoftware.snapseed.views.ItemSelectorView;
import com.niksoftware.snapseed.views.ItemSelectorView.OnClickListener;
import com.niksoftware.snapseed.views.ItemSelectorView.OnVisibilityChangeListener;

public class VintageController extends EmptyFilterController {
    private static final int[] ADJUSTABLE_FILTER_PARAMETERS = new int[]{0, 2, 104, 4, 9};
    private static final int[] STYLE_PREVIEW_RES_IDS = new int[]{R.drawable.icon_fo_vintage_style_1_default, R.drawable.icon_fo_vintage_style_1_active, R.drawable.icon_fo_vintage_style_2_default, R.drawable.icon_fo_vintage_style_2_active, R.drawable.icon_fo_vintage_style_3_default, R.drawable.icon_fo_vintage_style_3_active, R.drawable.icon_fo_vintage_style_4_default, R.drawable.icon_fo_vintage_style_4_active, R.drawable.icon_fo_vintage_style_5_default, R.drawable.icon_fo_vintage_style_5_active, R.drawable.icon_fo_vintage_style_6_default, R.drawable.icon_fo_vintage_style_6_active, R.drawable.icon_fo_vintage_style_7_default, R.drawable.icon_fo_vintage_style_7_active, R.drawable.icon_fo_vintage_style_8_default, R.drawable.icon_fo_vintage_style_8_active, R.drawable.icon_fo_vintage_style_9_default, R.drawable.icon_fo_vintage_style_9_active};
    private static final int[] TEXTURE_PREVIEW_RES_IDS = new int[]{R.drawable.icon_fo_vintage_texture_1_default, R.drawable.icon_fo_vintage_texture_1_active, R.drawable.icon_fo_vintage_texture_2_default, R.drawable.icon_fo_vintage_texture_2_active, R.drawable.icon_fo_vintage_texture_3_default, R.drawable.icon_fo_vintage_texture_3_active, R.drawable.icon_fo_vintage_texture_4_default, R.drawable.icon_fo_vintage_texture_4_active};
    private ItemSelectorView _itemSelectorView;
    private StaticStyleItemListAdapter _styleAdapter;
    private BaseFilterButton _styleButton;
    private OnClickListener _styleClickListener;
    private StaticStyleItemListAdapter _textureAdapter;
    private BaseFilterButton _textureButton;
    private OnClickListener _textureClickListener;

    private class StyleItemOnClickListener implements OnClickListener {
        private StyleItemOnClickListener() {
        }

        public boolean onItemClick(Integer itemId) {
            if (!itemId.equals(VintageController.this._styleAdapter.getActiveItemId())) {
                TrackerData.getInstance().usingParameter(3, false);
                VintageController.this.changeParameter(VintageController.this.getFilterParameter(), 3, itemId.intValue());
                VintageController.this._styleAdapter.setActiveItemId(itemId);
                VintageController.this._itemSelectorView.refreshSelectorItems(VintageController.this._styleAdapter, true);
            }
            return true;
        }

        public boolean onContextButtonClick() {
            return false;
        }
    }

    private class TextureSelectorOnClickListener implements OnClickListener {
        private TextureSelectorOnClickListener() {
        }

        public boolean onItemClick(Integer itemId) {
            if (itemId.equals(VintageController.this._textureAdapter.getActiveItemId())) {
                VintageController.this.randomizeParameter(101);
            } else {
                TrackerData.getInstance().usingParameter(101, false);
                VintageController.this.changeParameter(VintageController.this.getFilterParameter(), 101, itemId.intValue());
                VintageController.this._textureAdapter.setActiveItemId(itemId);
                VintageController.this._itemSelectorView.refreshSelectorItems(VintageController.this._textureAdapter, true);
            }
            return true;
        }

        public boolean onContextButtonClick() {
            return false;
        }
    }

    public void init(ControllerContext context) {
        super.init(context);
        addCircleHandler();
        addParameterHandler();
        FilterParameter filter = getFilterParameter();
        this._styleAdapter = new StaticStyleItemListAdapter(filter, 3, STYLE_PREVIEW_RES_IDS);
        this._styleClickListener = new StyleItemOnClickListener();
        this._textureAdapter = new StaticStyleItemListAdapter(filter, 101, TEXTURE_PREVIEW_RES_IDS);
        this._textureClickListener = new TextureSelectorOnClickListener();
        this._itemSelectorView = getItemSelectorView();
        this._itemSelectorView.setOnVisibilityChangeListener(new OnVisibilityChangeListener() {
            public void onVisibilityChanged(boolean isVisible) {
                if (!isVisible) {
                    VintageController.this._styleButton.setSelected(false);
                    VintageController.this._textureButton.setSelected(false);
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
        return 8;
    }

    public int[] getGlobalAdjustmentParameters() {
        return ADJUSTABLE_FILTER_PARAMETERS;
    }

    public boolean showsParameterView() {
        return true;
    }

    public boolean initLeftFilterButton(BaseFilterButton button) {
        if (button == null) {
            this._styleButton = null;
            return false;
        }
        this._styleButton = button;
        this._styleButton.setStateImages((int) R.drawable.icon_tb_style_default, (int) R.drawable.icon_tb_style_active, 0);
        this._styleButton.setText(getButtonTitle(R.string.style));
        this._styleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                VintageController.this._styleButton.setSelected(true);
                VintageController.this._styleAdapter.setActiveItemId(Integer.valueOf(VintageController.this.getFilterParameter().getParameterValueOld(3)));
                VintageController.this._itemSelectorView.reloadSelector(VintageController.this._styleAdapter, VintageController.this._styleClickListener);
                VintageController.this._itemSelectorView.setVisible(true, true);
            }
        });
        return true;
    }

    public boolean initRightFilterButton(BaseFilterButton button) {
        if (button == null) {
            this._textureButton = null;
            return false;
        }
        this._textureButton = button;
        this._textureButton.setStateImages((int) R.drawable.icon_tb_texture_default, (int) R.drawable.icon_tb_texture_active, 0);
        this._textureButton.setText(getButtonTitle(R.string.texture_label));
        this._textureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                VintageController.this._textureButton.setSelected(true);
                VintageController.this._textureAdapter.setActiveItemId(Integer.valueOf(VintageController.this.getFilterParameter().getParameterValueOld(101)));
                VintageController.this._itemSelectorView.reloadSelector(VintageController.this._textureAdapter, VintageController.this._textureClickListener);
                VintageController.this._itemSelectorView.setVisible(true, true);
            }
        });
        return true;
    }

    public BaseFilterButton getLeftFilterButton() {
        return this._styleButton;
    }

    public BaseFilterButton getRightFilterButton() {
        return this._textureButton;
    }

    public int getHelpResourceId() {
        return R.xml.overlay_vintage;
    }
}

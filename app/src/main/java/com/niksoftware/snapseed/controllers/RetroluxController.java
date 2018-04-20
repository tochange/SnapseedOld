package com.niksoftware.snapseed.controllers;

import android.view.View;

import com.niksoftware.snapseed.controllers.adapters.RetroluxPresetItemListAdapter;
import com.niksoftware.snapseed.controllers.adapters.StaticStyleItemListAdapter;
import com.niksoftware.snapseed.core.NativeCore;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.BaseFilterButton;
import com.niksoftware.snapseed.views.ItemSelectorView;
import com.niksoftware.snapseed.views.ItemSelectorView.OnClickListener;
import com.niksoftware.snapseed.views.ItemSelectorView.OnVisibilityChangeListener;

public class RetroluxController extends AutoshuffleFilterController implements OnClickListener {
    private static final int[] ADJUSTABLE_FILTER_PARAMETERS = new int[]{0, 2, 1, 9, 233, 232};
    private static final int[] STYLE_PREVIEW_RES_IDS = new int[]{R.drawable.icon_fo_retrolux_1_default, R.drawable.icon_fo_retrolux_1_active, R.drawable.icon_fo_retrolux_2_default, R.drawable.icon_fo_retrolux_2_active, R.drawable.icon_fo_retrolux_3_default, R.drawable.icon_fo_retrolux_3_active, R.drawable.icon_fo_retrolux_4_default, R.drawable.icon_fo_retrolux_4_active, R.drawable.icon_fo_retrolux_5_default, R.drawable.icon_fo_retrolux_5_active, R.drawable.icon_fo_retrolux_6_default, R.drawable.icon_fo_retrolux_6_active, R.drawable.icon_fo_retrolux_7_default, R.drawable.icon_fo_retrolux_7_active, R.drawable.icon_fo_retrolux_8_default, R.drawable.icon_fo_retrolux_8_active, R.drawable.icon_fo_retrolux_9_default, R.drawable.icon_fo_retrolux_9_active, R.drawable.icon_fo_retrolux_10_default, R.drawable.icon_fo_retrolux_10_active, R.drawable.icon_fo_retrolux_11_default, R.drawable.icon_fo_retrolux_11_active, R.drawable.icon_fo_retrolux_12_default, R.drawable.icon_fo_retrolux_12_active, R.drawable.icon_fo_retrolux_13_default, R.drawable.icon_fo_retrolux_13_active};
    private ItemSelectorView itemSelectorView;
    private RetroluxPresetItemListAdapter presetListAdapter;
    private BaseFilterButton shuffleButton;
    private BaseFilterButton styleButton;
    private StaticStyleItemListAdapter styleListAdapter;

    private class PresetSelectorOnClickListener implements OnClickListener {
        private PresetSelectorOnClickListener() {
        }

        public boolean onItemClick(Integer itemId) {
            if (itemId == null) {
                return false;
            }
            boolean isLeakItemClicked = RetroluxController.this.presetListAdapter.isLeakItem(itemId);
            RetroluxController.this.presetListAdapter.setActiveItem(itemId);
            RetroluxController.this.itemSelectorView.refreshSelectorItems(RetroluxController.this.presetListAdapter, true);
            FilterParameter filter = RetroluxController.this.getFilterParameter();
            if (isLeakItemClicked) {
                TrackerData.getInstance().usingParameter(234, false);
                RetroluxController.this.changeParameter(filter, 234, RetroluxController.this.presetListAdapter.getActiveLeakIndex() - 1, true, new Runnable() {
                    public void run() {
                        NativeCore.contextAction(RetroluxController.this.getFilterParameter(), 4);
                    }
                });
                return true;
            }
            TrackerData.getInstance().usingParameter(235, false);
            RetroluxController.this.changeParameter(filter, 235, RetroluxController.this.presetListAdapter.getActiveScratchIndex(), true, new Runnable() {
                public void run() {
                    NativeCore.contextAction(RetroluxController.this.getFilterParameter(), 5);
                }
            });
            return true;
        }

        public boolean onContextButtonClick() {
            if (RetroluxController.this.itemSelectorView.popSelectorState(true)) {
                RetroluxController.this.presetListAdapter = null;
            }
            return true;
        }
    }

    public void init(ControllerContext context) {
        super.init(context);
        addParameterHandler();
        this.styleListAdapter = new StaticStyleItemListAdapter(getFilterParameter(), 3, STYLE_PREVIEW_RES_IDS);
        this.styleListAdapter.setContextButtonAppearance(R.drawable.icon_fo_options_default, R.string.properties);
        this.itemSelectorView = getItemSelectorView();
        this.itemSelectorView.reloadSelector(this.styleListAdapter, this);
        this.itemSelectorView.setOnVisibilityChangeListener(new OnVisibilityChangeListener() {
            public void onVisibilityChanged(boolean isVisible) {
                RetroluxController.this.styleButton.setSelected(isVisible);
            }
        });
    }

    public void cleanup() {
        getEditingToolbar().itemSelectorWillHide();
        this.itemSelectorView.setVisible(false, false);
        this.itemSelectorView.cleanup();
        this.itemSelectorView = null;
        super.cleanup();
    }

    public int getFilterType() {
        return 16;
    }

    public int[] getGlobalAdjustmentParameters() {
        return ADJUSTABLE_FILTER_PARAMETERS;
    }

    public boolean initLeftFilterButton(BaseFilterButton button) {
        if (button == null) {
            this.styleButton = null;
            return false;
        }
        this.styleButton = button;
        this.styleButton.setStateImages((int) R.drawable.icon_tb_style_default, (int) R.drawable.icon_tb_style_active, 0);
        this.styleButton.setText(getButtonTitle(R.string.style));
        this.styleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                RetroluxController.this.updateStyleDataSource();
                RetroluxController.this.itemSelectorView.refreshSelectorItems(RetroluxController.this.styleListAdapter, true);
                RetroluxController.this.itemSelectorView.setVisible(true, true);
            }
        });
        return true;
    }

    public boolean initRightFilterButton(BaseFilterButton button) {
        if (button == null) {
            this.shuffleButton = null;
            return false;
        }
        this.shuffleButton = button;
        this.shuffleButton.setStateImages((int) R.drawable.icon_tb_shuffle_default, 0, 0);
        this.shuffleButton.setText(getButtonTitle(R.string.shuffle));
        this.shuffleButton.setStyle(R.style.EditToolbarButtonTitle);
        this.shuffleButton.setBackgroundResource(R.drawable.tb_button_background);
        this.shuffleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (RetroluxController.this.itemSelectorView.getVisibility() != 0) {
                    RetroluxController.this.randomize();
                    RetroluxController.this.updateStyleDataSource();
                }
            }
        });
        return true;
    }

    public BaseFilterButton getLeftFilterButton() {
        return this.styleButton;
    }

    public BaseFilterButton getRightFilterButton() {
        return this.shuffleButton;
    }

    public boolean showsParameterView() {
        return true;
    }

    public void undoRedoStateChanged() {
        updateStyleDataSource();
        super.undoRedoStateChanged();
    }

    private synchronized void updateStyleDataSource() {
        if (this.presetListAdapter != null) {
            this.itemSelectorView.popSelectorState(false);
            this.presetListAdapter = null;
        }
        this.styleListAdapter.setActiveItemId(Integer.valueOf(getFilterParameter().getParameterValueOld(3)));
    }

    public boolean onItemClick(Integer itemId) {
        if (itemId.equals(this.styleListAdapter.getActiveItemId())) {
            randomizeParameter(3);
        } else {
            FilterParameter filter = getFilterParameter();
            TrackerData.getInstance().usingParameter(3, false);
            filter.setParameterValueOld(3, itemId.intValue());
            NativeCore.contextAction(filter, 6);
            changeParameter(filter, 3, itemId.intValue(), true, null);
            this.styleListAdapter.setActiveItemId(itemId);
            this.itemSelectorView.refreshSelectorItems(this.styleListAdapter, true);
        }
        return true;
    }

    public boolean onContextButtonClick() {
        Integer activeStyleId = this.styleListAdapter.getActiveItemId();
        if (activeStyleId == null) {
            return false;
        }
        this.presetListAdapter = new RetroluxPresetItemListAdapter(getContext(), activeStyleId.intValue());
        FilterParameter filter = getFilterParameter();
        int leakIndex = filter.getParameterValueOld(234);
        this.presetListAdapter.setActiveItems(filter.getParameterValueOld(235), leakIndex < 0 ? 0 : leakIndex + 1);
        if (this.itemSelectorView.pushSelectorState(this.presetListAdapter)) {
            this.itemSelectorView.setSelectorOnClickListener(new PresetSelectorOnClickListener());
            return true;
        }
        this.presetListAdapter = null;
        return false;
    }
}

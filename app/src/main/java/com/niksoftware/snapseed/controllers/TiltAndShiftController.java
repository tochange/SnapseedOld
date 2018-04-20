package com.niksoftware.snapseed.controllers;

import android.view.View;

import com.niksoftware.snapseed.controllers.adapters.StaticStyleItemListAdapter;
import com.niksoftware.snapseed.controllers.touchhandlers.TiltShiftHotspotHandler;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.BaseFilterButton;
import com.niksoftware.snapseed.views.ItemSelectorView;
import com.niksoftware.snapseed.views.ItemSelectorView.OnClickListener;
import com.niksoftware.snapseed.views.ItemSelectorView.OnVisibilityChangeListener;

public class TiltAndShiftController extends EmptyFilterController {
    private static final int[] ADJUSTABLE_FILTER_PARAMETERS = new int[]{17, 19, 0, 2, 1};
    private static final int[] STYLE_PREVIEW_RES_IDS = new int[]{R.drawable.icon_fo_tiltshift_linear_default, R.drawable.icon_fo_tiltshift_linear_active, R.drawable.icon_fo_tiltshift_circular_default, R.drawable.icon_fo_tiltshift_circular_active};
    private ItemSelectorView _itemSelectorView;
    private StaticStyleItemListAdapter _styleAdapter;
    private BaseFilterButton _tiltShiftButton;
    private TiltShiftHotspotHandler _touchListener;

    private class SelectorItemOnClickListener implements OnClickListener {
        private SelectorItemOnClickListener() {
        }

        public boolean onItemClick(Integer itemId) {
            if (TiltAndShiftController.this.changeParameter(TiltAndShiftController.this.getFilterParameter(), 3, itemId.intValue())) {
                TrackerData.getInstance().usingParameter(234, false);
                TiltAndShiftController.this._styleAdapter.setActiveItemId(itemId);
                TiltAndShiftController.this._itemSelectorView.refreshSelectorItems(TiltAndShiftController.this._styleAdapter, true);
            }
            return true;
        }

        public boolean onContextButtonClick() {
            return false;
        }
    }

    public void init(ControllerContext context) {
        super.init(context);
        this._touchListener = new TiltShiftHotspotHandler();
        addTouchListener(this._touchListener);
        addParameterHandler();
        FilterParameter filter = getFilterParameter();
        this._styleAdapter = new StaticStyleItemListAdapter(filter, 3, STYLE_PREVIEW_RES_IDS);
        this._styleAdapter.setActiveItemId(Integer.valueOf(filter.getParameterValueOld(3)));
        this._itemSelectorView = getItemSelectorView();
        this._itemSelectorView.reloadSelector(this._styleAdapter);
        this._itemSelectorView.setSelectorOnClickListener(new SelectorItemOnClickListener());
        this._itemSelectorView.setOnVisibilityChangeListener(new OnVisibilityChangeListener() {
            public void onVisibilityChanged(boolean isVisible) {
                TiltAndShiftController.this._tiltShiftButton.setSelected(isVisible);
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
        return 14;
    }

    public boolean initLeftFilterButton(BaseFilterButton button) {
        if (button == null) {
            this._tiltShiftButton = null;
            return false;
        }
        this._tiltShiftButton = button;
        this._tiltShiftButton.setStateImages((int) R.drawable.icon_tb_style_default, (int) R.drawable.icon_tb_style_active, 0);
        this._tiltShiftButton.setText(getButtonTitle(R.string.style));
        this._tiltShiftButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                TiltAndShiftController.this._styleAdapter.setActiveItemId(Integer.valueOf(TiltAndShiftController.this.getFilterParameter().getParameterValueOld(3)));
                TiltAndShiftController.this._itemSelectorView.refreshSelectorItems(TiltAndShiftController.this._styleAdapter, true);
                TiltAndShiftController.this._itemSelectorView.setVisible(true, true);
            }
        });
        return true;
    }

    public BaseFilterButton getLeftFilterButton() {
        return this._tiltShiftButton;
    }

    public int[] getGlobalAdjustmentParameters() {
        return ADJUSTABLE_FILTER_PARAMETERS;
    }

    public boolean showsParameterView() {
        return true;
    }

    public int getHelpResourceId() {
        return R.xml.overlay_tilt_shift;
    }

    public void onPause() {
        super.onPause();
        this._touchListener.handlePinchAbort();
        this._touchListener.handleTouchAbort(false);
    }

    public void onResume() {
        this._touchListener.handlePinchAbort();
        this._touchListener.handleTouchAbort(false);
    }
}

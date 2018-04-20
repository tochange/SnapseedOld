package com.niksoftware.snapseed.controllers;

import android.graphics.Bitmap;
import android.view.View;

import com.niksoftware.snapseed.controllers.adapters.StyleItemListAdapter;
import com.niksoftware.snapseed.controllers.touchhandlers.CenterFocusHotspotHandler;
import com.niksoftware.snapseed.controllers.touchhandlers.TouchHandler;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.BaseFilterButton;
import com.niksoftware.snapseed.views.ItemSelectorView;
import com.niksoftware.snapseed.views.ItemSelectorView.OnClickListener;
import com.niksoftware.snapseed.views.ItemSelectorView.OnVisibilityChangeListener;
import java.util.List;

public class CenterFocusController extends EmptyFilterController {
    private static final int[] ADJUSTABLE_FILTER_PARAMETERS = new int[]{19, 23, 22};
    private CenterFocusHotspotHandler centerFocusHotspotHandler;
    private NotificationCenterListener didChangeFilterParameterValue;
    private ItemSelectorView itemSelectorView;
    private BaseFilterButton presetButton;
    private StyleItemListAdapter styleListAdapter;
    private BaseFilterButton weakStrongButton;

    private class StyleSelectorOnClickListener implements OnClickListener {
        private StyleSelectorOnClickListener() {
        }

        public boolean onItemClick(Integer itemId) {
            if (CenterFocusController.this.changeParameter(CenterFocusController.this.getFilterParameter(), 3, itemId.intValue())) {
                TrackerData.getInstance().usingParameter(3, false);
                CenterFocusController.this.styleListAdapter.setActiveItemId(itemId);
                CenterFocusController.this.itemSelectorView.refreshSelectorItems(CenterFocusController.this.styleListAdapter, true);
            }
            return true;
        }

        public boolean onContextButtonClick() {
            return false;
        }
    }

    public void init(ControllerContext context) {
        super.init(context);
        TouchHandler centerFocusHotspotHandler = new CenterFocusHotspotHandler();
        this.centerFocusHotspotHandler = centerFocusHotspotHandler;
        addTouchListener(centerFocusHotspotHandler);
        addParameterHandler();
        this.didChangeFilterParameterValue = new NotificationCenterListener() {
            public void performAction(Object arg) {
                if (arg != null && ((Integer) arg).intValue() == 3) {
                    CenterFocusController.this.updateWeakStrongState();
                }
            }
        };
        NotificationCenter.getInstance().addListener(this.didChangeFilterParameterValue, ListenerType.DidChangeFilterParameterValue);
        this.styleListAdapter = new StyleItemListAdapter(getContext(), getFilterParameter(), 3, getTilesProvider().getStyleSourceImage());
        this.itemSelectorView = getItemSelectorView();
        this.itemSelectorView.reloadSelector(this.styleListAdapter);
        this.itemSelectorView.setSelectorOnClickListener(new StyleSelectorOnClickListener());
        this.itemSelectorView.setOnVisibilityChangeListener(new OnVisibilityChangeListener() {
            public void onVisibilityChanged(boolean isVisible) {
                CenterFocusController.this.presetButton.setSelected(isVisible);
            }
        });
    }

    public void cleanup() {
        getEditingToolbar().itemSelectorWillHide();
        this.itemSelectorView.setVisible(false, false);
        this.itemSelectorView.cleanup();
        this.itemSelectorView = null;
        NotificationCenter.getInstance().removeListener(this.didChangeFilterParameterValue, ListenerType.DidChangeFilterParameterValue);
        super.cleanup();
    }

    public int getFilterType() {
        return 11;
    }

    public int[] getGlobalAdjustmentParameters() {
        return ADJUSTABLE_FILTER_PARAMETERS;
    }

    public boolean showsParameterView() {
        return true;
    }

    public boolean initLeftFilterButton(BaseFilterButton button) {
        if (button == null) {
            this.presetButton = null;
            return false;
        }
        this.presetButton = button;
        this.presetButton.setStateImages((int) R.drawable.icon_tb_presets_default, (int) R.drawable.icon_tb_presets_active, 0);
        this.presetButton.setText(getButtonTitle(R.string.presets));
        this.presetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                CenterFocusController.this.styleListAdapter.setActiveItemId(Integer.valueOf(CenterFocusController.this.getFilterParameter().getParameterValueOld(3)));
                CenterFocusController.this.itemSelectorView.refreshSelectorItems(CenterFocusController.this.styleListAdapter, true);
                CenterFocusController.this.itemSelectorView.setVisible(true, true);
                CenterFocusController.this.previewImages(3);
            }
        });
        return true;
    }

    public boolean initRightFilterButton(BaseFilterButton button) {
        if (button == null) {
            this.weakStrongButton = null;
            return false;
        }
        this.weakStrongButton = button;
        this.weakStrongButton.setStyle(R.style.EditToolbarButtonTitle.Selectable.PreserveTitleColor);
        this.weakStrongButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CenterFocusController.this.toggleWeakStrong();
            }
        });
        updateWeakStrongState();
        return true;
    }

    public BaseFilterButton getLeftFilterButton() {
        return this.presetButton;
    }

    public BaseFilterButton getRightFilterButton() {
        return this.weakStrongButton;
    }

    public void toggleWeakStrong() {
        FilterParameter filter = getFilterParameter();
        if (changeParameter(filter, 12, filter.getParameterValueOld(12) != 0 ? 0 : 1)) {
            updateWeakStrongState();
        }
    }

    public void undoRedoStateChanged() {
        super.undoRedoStateChanged();
        updateWeakStrongState();
    }

    private void updateWeakStrongState() {
        this.weakStrongButton.setSelected(getFilterParameter().getParameterValueOld(12) > 0);
        if (this.weakStrongButton.isSelected()) {
            this.weakStrongButton.setStateImages((int) R.drawable.icon_tb_status_strong_active, (int) R.drawable.icon_tb_status_strong_active, 0);
            this.weakStrongButton.setText(getButtonTitle(R.string.strong));
            return;
        }
        this.weakStrongButton.setStateImages((int) R.drawable.icon_tb_status_weak_default, (int) R.drawable.icon_tb_status_weak_default, 0);
        this.weakStrongButton.setText(getButtonTitle(R.string.weak));
    }

    public int getHelpResourceId() {
        return R.xml.overlay_two_gestures_on_image;
    }

    public void onPause() {
        this.centerFocusHotspotHandler.handlePinchAbort();
    }

    public void setPreviewImage(List<Bitmap> images, int parameter) {
        if (this.styleListAdapter.updateStylePreviews(images)) {
            this.itemSelectorView.refreshSelectorItems(this.styleListAdapter, false);
        }
    }
}

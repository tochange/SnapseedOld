package com.niksoftware.snapseed.controllers;

import android.view.View;

import com.niksoftware.snapseed.controllers.adapters.StaticStyleItemListAdapter;
import com.niksoftware.snapseed.controllers.touchhandlers.CircleHandler;
import com.niksoftware.snapseed.controllers.touchhandlers.HotspotHandler;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.UndoObject;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.BaseFilterButton;
import com.niksoftware.snapseed.views.ItemSelectorView;
import com.niksoftware.snapseed.views.ItemSelectorView.OnClickListener;
import com.niksoftware.snapseed.views.ItemSelectorView.OnVisibilityChangeListener;

public class GrungeController extends AutoshuffleFilterController {
    private static final int[] ADJUSTABLE_FILTER_PARAMETERS = new int[]{3, 0, 1, 104, 2};
    private static final int[] TEXTURE_ITEM_IDS = new int[]{101, 102, 103, 104, 105};
    private static final int[] TEXTURE_PREVIEW_RES_IDS = new int[]{R.drawable.icon_fo_grunge_texture_1_default, R.drawable.icon_fo_grunge_texture_1_active, R.drawable.icon_fo_grunge_texture_2_default, R.drawable.icon_fo_grunge_texture_2_active, R.drawable.icon_fo_grunge_texture_3_default, R.drawable.icon_fo_grunge_texture_3_active, R.drawable.icon_fo_grunge_texture_4_default, R.drawable.icon_fo_grunge_texture_4_active, R.drawable.icon_fo_grunge_texture_5_default, R.drawable.icon_fo_grunge_texture_5_active};
    private ItemSelectorView _itemSelectorView;
    private BaseFilterButton _shuffleButton;
    private BaseFilterButton _textureButton;
    private StaticStyleItemListAdapter _textureListAdapter;

    private static class GrungeHotspotHandler extends HotspotHandler {
        private NotificationCenterListener _undoRedoPerformed;

        public GrungeHotspotHandler() {
            NotificationCenter instance = NotificationCenter.getInstance();
            NotificationCenterListener anonymousClass1 = new NotificationCenterListener() {
                public void performAction(Object arg) {
                    if (arg != null && ((UndoObject) arg).getChangedParameter() == 5) {
                        GrungeHotspotHandler.this.updateHotspotPosition();
                        GrungeHotspotHandler.this.hideHotspotDelayed();
                    }
                }
            };
            this._undoRedoPerformed = anonymousClass1;
            instance.addListener(anonymousClass1, ListenerType.UndoRedoPerformed);
        }

        public void cleanup() {
            NotificationCenter.getInstance().removeListener(this._undoRedoPerformed, ListenerType.UndoRedoPerformed);
            super.cleanup();
        }

        protected float calcRadius(int radius) {
            return (((float) radius) / 100.0f) + 0.4f;
        }

        protected int setParameterValue(float startPinchSize, float oldParamValue, float currentPinchSize) {
            return (int) CircleHandler.calcUpdatedValue(0.0f, 100.0f, startPinchSize, currentPinchSize, oldParamValue, CircleHandler.PINCH_FEEDBACK_RATIO);
        }

        protected boolean circleOnWhenTouchUp() {
            return false;
        }
    }

    private class TextureSelectorOnClickListener implements OnClickListener {
        private TextureSelectorOnClickListener() {
        }

        public boolean onItemClick(Integer itemId) {
            if (itemId.equals(GrungeController.this._textureListAdapter.getActiveItemId())) {
                GrungeController.this.randomizeParameter(101);
            } else if (GrungeController.this.changeParameter(GrungeController.this.getFilterParameter(), 101, itemId.intValue())) {
                TrackerData.getInstance().usingParameter(101, false);
                GrungeController.this._textureListAdapter.setActiveItemId(itemId);
                GrungeController.this._itemSelectorView.refreshSelectorItems(GrungeController.this._textureListAdapter, true);
            }
            return true;
        }

        public boolean onContextButtonClick() {
            return false;
        }
    }

    public void init(ControllerContext context) {
        super.init(context);
        addTouchListener(new GrungeHotspotHandler());
        addParameterHandler();
        this._textureListAdapter = new StaticStyleItemListAdapter(getFilterParameter(), 101, TEXTURE_PREVIEW_RES_IDS, TEXTURE_ITEM_IDS);
        this._itemSelectorView = getItemSelectorView();
        this._itemSelectorView.reloadSelector(this._textureListAdapter);
        this._itemSelectorView.setSelectorOnClickListener(new TextureSelectorOnClickListener());
        this._itemSelectorView.setOnVisibilityChangeListener(new OnVisibilityChangeListener() {
            public void onVisibilityChanged(boolean isVisible) {
                GrungeController.this._textureButton.setSelected(isVisible);
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
        return 10;
    }

    public boolean initLeftFilterButton(BaseFilterButton button) {
        if (button == null) {
            this._shuffleButton = null;
            return false;
        }
        this._shuffleButton = button;
        this._shuffleButton.setStateImages((int) R.drawable.icon_tb_shuffle_default, 0, 0);
        this._shuffleButton.setText(getButtonTitle(R.string.shuffle));
        this._shuffleButton.setStyle(R.style.EditToolbarButtonTitle);
        this._shuffleButton.setBackgroundResource(R.drawable.tb_button_background);
        this._shuffleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                GrungeController.this.randomize();
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
        this._textureButton.setStyle(R.style.EditToolbarButtonTitle.Selectable);
        this._textureButton.setBackgroundDrawable(null);
        this._textureButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                GrungeController.this._textureListAdapter.setActiveItemId(Integer.valueOf(GrungeController.this.getFilterParameter().getParameterValueOld(101)));
                GrungeController.this._itemSelectorView.refreshSelectorItems(GrungeController.this._textureListAdapter, true);
                GrungeController.this._itemSelectorView.setVisible(true, true);
            }
        });
        return true;
    }

    public BaseFilterButton getLeftFilterButton() {
        return this._shuffleButton;
    }

    public BaseFilterButton getRightFilterButton() {
        return this._textureButton;
    }

    public int[] getGlobalAdjustmentParameters() {
        return ADJUSTABLE_FILTER_PARAMETERS;
    }

    public boolean showsParameterView() {
        return true;
    }

    public int getHelpResourceId() {
        return R.xml.overlay_grunge;
    }
}

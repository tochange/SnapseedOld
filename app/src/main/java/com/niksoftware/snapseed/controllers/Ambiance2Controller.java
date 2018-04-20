package com.niksoftware.snapseed.controllers;

import android.view.View;

import com.niksoftware.snapseed.controllers.adapters.StaticStyleItemListAdapter;
import com.niksoftware.snapseed.core.NativeCore;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.BaseFilterButton;
import com.niksoftware.snapseed.views.ItemSelectorView;
import com.niksoftware.snapseed.views.ItemSelectorView.OnClickListener;
import com.niksoftware.snapseed.views.ItemSelectorView.OnVisibilityChangeListener;

public class Ambiance2Controller extends EmptyFilterController {
    private static final int[] FILTER_PARAMETERS = new int[]{12, 0, 2, 656};
    private static final int[] STYLE_PREVIEW_RES_IDS = new int[]{R.drawable.icon_fo_nature_default, R.drawable.icon_fo_nature_active, R.drawable.icon_fo_people_default, R.drawable.icon_fo_people_active, R.drawable.icon_fo_fine_default, R.drawable.icon_fo_fine_active, R.drawable.icon_fo_strong_default, R.drawable.icon_fo_strong_active};
    private ItemSelectorView itemSelectorView;
    private BaseFilterButton styleButton;
    private OnClickListener styleClickListener;
    private StaticStyleItemListAdapter styleListAdapter;

    private class StyleSelectorOnClickListener implements OnClickListener {
        private StyleSelectorOnClickListener() {
        }

        public boolean onItemClick(Integer itemId) {
            if (!itemId.equals(Ambiance2Controller.this.styleListAdapter.getActiveItemId())) {
                FilterParameter param = Ambiance2Controller.this.getFilterParameter();
                TrackerData.getInstance().usingParameter(3, false);
                param.setParameterValueOld(3, itemId.intValue());
                NativeCore.contextAction(param, 6);
                Ambiance2Controller.this.changeParameter(param, 3, itemId.intValue(), true, null);
                Ambiance2Controller.this.styleListAdapter.setActiveItemId(itemId);
                Ambiance2Controller.this.itemSelectorView.refreshSelectorItems(Ambiance2Controller.this.styleListAdapter, true);
            }
            return true;
        }

        public boolean onContextButtonClick() {
            return false;
        }
    }

    public void init(ControllerContext context) {
        super.init(context);
        addParameterHandler();
        this.styleListAdapter = new StaticStyleItemListAdapter(getFilterParameter(), 3, STYLE_PREVIEW_RES_IDS);
        this.styleClickListener = new StyleSelectorOnClickListener();
        this.itemSelectorView = getItemSelectorView();
        this.itemSelectorView.setOnVisibilityChangeListener(new OnVisibilityChangeListener() {
            public void onVisibilityChanged(boolean isVisible) {
                Ambiance2Controller.this.styleButton.setSelected(isVisible);
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
        return 100;
    }

    public int[] getGlobalAdjustmentParameters() {
        return FILTER_PARAMETERS;
    }

    public boolean showsParameterView() {
        return true;
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
                Ambiance2Controller.this.styleListAdapter.setActiveItemId(Integer.valueOf(Ambiance2Controller.this.getFilterParameter().getParameterValueOld(3)));
                Ambiance2Controller.this.itemSelectorView.reloadSelector(Ambiance2Controller.this.styleListAdapter, Ambiance2Controller.this.styleClickListener);
                Ambiance2Controller.this.itemSelectorView.setVisible(true, true);
            }
        });
        return true;
    }

    public int getHelpResourceId() {
        return R.xml.overlay_two_gestures;
    }
}

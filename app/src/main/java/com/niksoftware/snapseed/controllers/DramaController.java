package com.niksoftware.snapseed.controllers;

import android.graphics.Bitmap;
import android.view.View;
import android.view.View.OnClickListener;

import com.niksoftware.snapseed.controllers.adapters.StyleItemListAdapter;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.BaseFilterButton;
import com.niksoftware.snapseed.views.ItemSelectorView;
import com.niksoftware.snapseed.views.ItemSelectorView.OnVisibilityChangeListener;
import java.util.List;

public class DramaController extends EmptyFilterController {
    private static final int[] ADJUSTABLE_FILTER_PARAMETERS = new int[]{12, 2};
    private ItemSelectorView itemSelectorView;
    private BaseFilterButton styleButton;
    private StyleItemListAdapter styleListAdapter;

    private class StyleButtonOnClickListener implements OnClickListener {
        private StyleButtonOnClickListener() {
        }

        public void onClick(View view) {
            DramaController.this.styleListAdapter.setActiveItemId(Integer.valueOf(DramaController.this.getFilterParameter().getParameterValueOld(3)));
            DramaController.this.itemSelectorView.refreshSelectorItems(DramaController.this.styleListAdapter, true);
            DramaController.this.itemSelectorView.setVisible(true, true);
            DramaController.this.previewImages(3);
        }
    }

    private class StyleItemOnClickListener implements ItemSelectorView.OnClickListener {
        private StyleItemOnClickListener() {
        }

        public boolean onItemClick(Integer itemId) {
            if (DramaController.this.changeParameter(DramaController.this.getFilterParameter(), 3, itemId.intValue())) {
                TrackerData.getInstance().usingParameter(3, false);
                DramaController.this.styleListAdapter.setActiveItemId(itemId);
                DramaController.this.itemSelectorView.refreshSelectorItems(DramaController.this.styleListAdapter, true);
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
        this.styleListAdapter = new StyleItemListAdapter(getContext(), getFilterParameter(), 3, getTilesProvider().getStyleSourceImage());
        this.itemSelectorView = getItemSelectorView();
        this.itemSelectorView.reloadSelector(this.styleListAdapter);
        this.itemSelectorView.setSelectorOnClickListener(new StyleItemOnClickListener());
        this.itemSelectorView.setOnVisibilityChangeListener(new OnVisibilityChangeListener() {
            public void onVisibilityChanged(boolean isVisible) {
                DramaController.this.styleButton.setSelected(isVisible);
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
        return 9;
    }

    public int[] getGlobalAdjustmentParameters() {
        return ADJUSTABLE_FILTER_PARAMETERS;
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
        this.styleButton.setOnClickListener(new StyleButtonOnClickListener());
        return true;
    }

    public BaseFilterButton getLeftFilterButton() {
        return this.styleButton;
    }

    public void setPreviewImage(List<Bitmap> images, int parameter) {
        if (this.styleListAdapter.updateStylePreviews(images)) {
            this.itemSelectorView.refreshSelectorItems(this.styleListAdapter, false);
        }
    }
}

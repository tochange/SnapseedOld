package com.niksoftware.snapseed.controllers;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;

import com.niksoftware.snapseed.controllers.adapters.StyleItemListAdapter;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.BaseFilterButton;
import com.niksoftware.snapseed.views.ItemSelectorView;
import com.niksoftware.snapseed.views.ItemSelectorView.OnClickListener;
import java.util.ArrayList;
import java.util.List;

public class FramesController extends EmptyFilterController {
    public static final int[] ADJUSTABLE_FILTER_PARAMETERS = new int[]{222, 221};
    private BaseFilterButton _frameShuffleButton;
    private ItemSelectorView _itemSelectorView;
    private int _previewSize;
    private BaseFilterButton _styleButton;
    private StyleItemListAdapter _styleListAdapter;

    private class StyleSelectorOnClickListener implements OnClickListener {
        private StyleSelectorOnClickListener() {
        }

        public boolean onItemClick(Integer itemId) {
            if (FramesController.this.changeParameter(FramesController.this.getFilterParameter(), 3, itemId.intValue())) {
                TrackerData.getInstance().usingParameter(3, false);
                FramesController.this._styleListAdapter.setActiveItemId(itemId);
                FramesController.this._itemSelectorView.refreshSelectorItems(FramesController.this._styleListAdapter, true);
            }
            return true;
        }

        public boolean onContextButtonClick() {
            FramesController.this.randomizeParameter(101);
            return true;
        }
    }

    public void init(ControllerContext controllerContext) {
        super.init(controllerContext);
        addParameterHandler();
        Context context = getContext();
        this._previewSize = context.getResources().getDimensionPixelSize(R.dimen.tb_subpanel_preview_size);
        this._styleListAdapter = new StyleItemListAdapter(context, getFilterParameter(), 3, getTilesProvider().getStyleSourceImage());
        this._styleListAdapter.setContextButtonAppearance(R.drawable.icon_fo_shuffle_default, R.string.properties);
        this._itemSelectorView = getItemSelectorView();
        this._itemSelectorView.reloadSelector(this._styleListAdapter);
        this._itemSelectorView.setSelectorOnClickListener(new StyleSelectorOnClickListener());
    }

    public void cleanup() {
        getEditingToolbar().itemSelectorWillHide();
        this._itemSelectorView.setVisible(false, false);
        this._itemSelectorView.cleanup();
        this._itemSelectorView = null;
        super.cleanup();
    }

    public boolean initLeftFilterButton(BaseFilterButton button) {
        if (button == null) {
            this._styleButton = null;
            return false;
        }
        this._styleButton = button;
        this._styleButton.setStateImages((int) R.drawable.icon_tb_style_default, (int) R.drawable.icon_tb_style_active, 0);
        this._styleButton.setText(getButtonTitle(R.string.style));
        this._styleButton.setStyle(R.style.EditToolbarButtonTitle.Selectable);
        this._styleButton.setBackgroundDrawable(null);
        this._styleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                FramesController.this._styleListAdapter.setActiveItemId(Integer.valueOf(FramesController.this.getFilterParameter().getParameterValueOld(3)));
                FramesController.this._itemSelectorView.refreshSelectorItems(FramesController.this._styleListAdapter, true);
                FramesController.this._itemSelectorView.setVisible(true, true);
                FramesController.this.previewImages(3);
            }
        });
        return true;
    }

    public boolean initRightFilterButton(BaseFilterButton button) {
        if (button == null) {
            this._frameShuffleButton = null;
            return false;
        }
        this._frameShuffleButton = button;
        this._frameShuffleButton.setStateImages((int) R.drawable.icon_tb_frameshuffle_default, 0, 0);
        this._frameShuffleButton.setText(getButtonTitle(R.string.shuffle));
        this._frameShuffleButton.setStyle(R.style.EditToolbarButtonTitle);
        this._frameShuffleButton.setBackgroundResource(R.drawable.tb_button_background);
        this._frameShuffleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FramesController.this.randomize();
            }
        });
        return true;
    }

    public BaseFilterButton getLeftFilterButton() {
        return this._styleButton;
    }

    public BaseFilterButton getRightFilterButton() {
        return this._frameShuffleButton;
    }

    public int[] getGlobalAdjustmentParameters() {
        return ADJUSTABLE_FILTER_PARAMETERS;
    }

    public int getFilterType() {
        return 12;
    }

    public boolean showsParameterView() {
        return true;
    }

    public void setPreviewImage(List<Bitmap> images, int parameter) {
        List<Bitmap> styleImages = new ArrayList();
        for (Bitmap source : images) {
            int size = Math.min(Math.min(this._previewSize, source.getWidth()), source.getHeight());
            styleImages.add(Bitmap.createBitmap(source, 0, 0, size, size));
        }
        if (this._styleListAdapter.updateStylePreviews(styleImages)) {
            this._itemSelectorView.refreshSelectorItems(this._styleListAdapter, false);
        }
    }

    protected int getStylePreviewSize() {
        return this._previewSize * 3;
    }

    public int getHelpResourceId() {
        return R.xml.overlay_frames;
    }
}

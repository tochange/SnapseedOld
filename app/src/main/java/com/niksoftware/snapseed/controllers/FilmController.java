package com.niksoftware.snapseed.controllers;

import android.graphics.Bitmap;
import android.view.View;

import com.niksoftware.snapseed.controllers.adapters.FilmItemListAdapter;
import com.niksoftware.snapseed.core.NativeCore;
import com.niksoftware.snapseed.core.filterparameters.FilmFilterParameter;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.BaseFilterButton;
import com.niksoftware.snapseed.views.ItemSelectorView;
import com.niksoftware.snapseed.views.ItemSelectorView.OnClickListener;
import java.util.List;

public class FilmController extends EmptyFilterController {
    private static final int[] GLOBAL_ADJUSTMENT_PARAMS = new int[]{9, 0, 2, 19, 6};
    private static final int[] GLOBAL_ADJUSTMENT_PARAMS_FOR_BW_STYLE = new int[]{9, 0, 19, 6};
    private ItemSelectorView itemSelectorView;
    private BaseFilterButton styleButton;
    private FilmItemListAdapter styleListAdapter;

    public void init(ControllerContext context) {
        super.init(context);
        addParameterHandler();
        this.styleListAdapter = new FilmItemListAdapter(getContext(), getFilterParameter(), 3, getTilesProvider().getStyleSourceImage());
        this.itemSelectorView = getItemSelectorView();
        this.itemSelectorView.reloadSelector(this.styleListAdapter);
        this.itemSelectorView.setSelectorOnClickListener(new OnClickListener() {
            public boolean onItemClick(Integer newStack) {
                FilmController.this.onItemClickHandler(newStack);
                return true;
            }

            public boolean onContextButtonClick() {
                return false;
            }
        });
    }

    private void onItemClickHandler(Integer newStack) {
        int style = getFilterParameter().getParameterValueOld(3);
        int currentStack = FilmFilterParameter.mapFilterStyleToStackNumber(style);
        if (newStack.intValue() == currentStack) {
            style++;
            if (style >= FilmFilterParameter.getPresetCountForStack(currentStack) + FilmFilterParameter.mapStackNumberToFilterStyle(currentStack)) {
                style = FilmFilterParameter.mapStackNumberToFilterStyle(newStack.intValue());
            }
        } else {
            style = FilmFilterParameter.mapStackNumberToFilterStyle(newStack.intValue());
        }
        if (changeParameter(getFilterParameter(), 3, style)) {
            TrackerData.getInstance().usingParameter(3, false);
            this.styleListAdapter.setActiveItemId(newStack);
            this.itemSelectorView.refreshSelectorItems(this.styleListAdapter, true);
            NativeCore.contextAction(getFilterParameter(), 6);
            getWorkingAreaView().requestRender();
            if (newStack.intValue() == 2 || currentStack == 2) {
                getWorkingAreaView().updateParameterView(getGlobalAdjustmentParameters());
            }
        }
    }

    public void cleanup() {
        getEditingToolbar().itemSelectorWillHide();
        this.itemSelectorView.setVisible(false, false);
        this.itemSelectorView.cleanup();
        this.itemSelectorView = null;
        super.cleanup();
    }

    public int getFilterType() {
        return 200;
    }

    public int[] getGlobalAdjustmentParameters() {
        FilterParameter filter = getFilterParameter();
        if (FilmFilterParameter.mapFilterStyleToStackNumber(filter.getParameterValueOld(3)) != 2) {
            return GLOBAL_ADJUSTMENT_PARAMS;
        }
        if (filter.getActiveFilterParameter() == 2) {
            filter.setActiveFilterParameter(filter.getDefaultParameter());
        }
        return GLOBAL_ADJUSTMENT_PARAMS_FOR_BW_STYLE;
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
        this.styleButton.setStateImages((int) R.drawable.icon_tb_presets_default, (int) R.drawable.icon_tb_presets_active, 0);
        this.styleButton.setText(getButtonTitle(R.string.style));
        this.styleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                FilmController.this.styleListAdapter.setActiveItemId(Integer.valueOf(FilmController.this.getFilterParameter().getParameterValueOld(3)));
                FilmController.this.itemSelectorView.refreshSelectorItems(FilmController.this.styleListAdapter, true);
                FilmController.this.itemSelectorView.setVisible(true, true);
                FilmController.this.previewImages(3);
            }
        });
        return true;
    }

    public BaseFilterButton getLeftFilterButton() {
        return this.styleButton;
    }

    public void setPreviewImage(List<Bitmap> images, int parameter) {
        this.styleListAdapter.updateStylePreviews(images);
        this.itemSelectorView.refreshSelectorItems(this.styleListAdapter, false);
    }
}

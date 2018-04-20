package com.niksoftware.snapseed.controllers;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.SparseArray;
import android.view.View;

import com.niksoftware.snapseed.core.RenderFilterInterface.OnBatchRenderListener;
import com.niksoftware.snapseed.core.rendering.TilesProvider;
import com.niksoftware.snapseed.views.BaseFilterButton;
import com.niksoftware.snapseed.views.ImageViewGL;
import java.util.List;

public class EmptyFilterController extends FilterController {
    protected SparseArray<List<Bitmap>> previewImages;

    private class StylePreviewOnRenderListener implements OnBatchRenderListener {
        private int styleFilterParameter;

        public StylePreviewOnRenderListener(int parameter) {
            this.styleFilterParameter = parameter;
        }

        public void onRenderProgressUpdate(int currentStage, int stageCount) {
        }

        public void onRenderCancelled() {
        }

        public void onRenderFinished(List<Bitmap> renderResult) {
            EmptyFilterController.this.setPreviewImage(renderResult, this.styleFilterParameter);
        }
    }

    public int getFilterType() {
        return 1;
    }

    public int[] getGlobalAdjustmentParameters() {
        return new int[0];
    }

    public boolean initLeftFilterButton(BaseFilterButton button) {
        return false;
    }

    public boolean initRightFilterButton(BaseFilterButton button) {
        return false;
    }

    public BaseFilterButton getLeftFilterButton() {
        return null;
    }

    public BaseFilterButton getRightFilterButton() {
        return null;
    }

    public void setPreviewImage(List<Bitmap> images, int parameter) {
        if (this.previewImages == null) {
            this.previewImages = new SparseArray();
        }
        this.previewImages.put(parameter, images);
    }

    public List<Bitmap> previewImages(int parameter) {
        List<Bitmap> backup = this.previewImages != null ? (List) this.previewImages.get(parameter) : null;
        loadStyleImages(parameter);
        return backup;
    }

    protected Rect getStylePreviewRect(int baseSize) {
        TilesProvider tilesProvider = getTilesProvider();
        Bitmap sourceImage = tilesProvider.getSourceImage();
        float ratio = ((float) sourceImage.getWidth()) / ((float) sourceImage.getHeight());
        int width = baseSize;
        int height = baseSize;
        if (((double) ratio) <= 1.0d) {
            height = Math.round(((float) width) / ratio);
        } else {
            width = Math.round(((float) height) * ratio);
        }
        if (width > tilesProvider.getPreviewWidth() || height > tilesProvider.getPreviewHeight()) {
            float scale = Math.max(((float) width) / ((float) tilesProvider.getPreviewWidth()), ((float) height) / ((float) tilesProvider.getPreviewHeight()));
            height = Math.round(((float) height) / scale);
            width = Math.round(((float) width) / scale);
        }
        return new Rect(0, 0, width, height);
    }

    protected int getStylePreviewSize() {
        return getResources().getDimensionPixelSize(R.dimen.tb_subpanel_preview_size);
    }

    protected final void loadStyleImages(int parameter) {
        View imageView = getWorkingAreaView().getImageView();
        if (imageView instanceof ImageViewGL) {
            Rect rect = getStylePreviewRect(getStylePreviewSize());
            ((ImageViewGL) imageView).requestRenderStyleImages(getTilesProvider(), rect.width(), rect.height(), getFilterParameter(), parameter, new StylePreviewOnRenderListener(parameter));
        }
    }
}

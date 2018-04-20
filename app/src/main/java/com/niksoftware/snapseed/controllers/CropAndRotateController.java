package com.niksoftware.snapseed.controllers;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.View;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.controllers.adapters.StaticStyleItemListAdapter;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.BaseFilterButton;
import com.niksoftware.snapseed.views.CropRotateView;
import com.niksoftware.snapseed.views.ItemSelectorView;
import com.niksoftware.snapseed.views.ItemSelectorView.OnClickListener;
import com.niksoftware.snapseed.views.ItemSelectorView.OnVisibilityChangeListener;
import com.niksoftware.snapseed.views.WorkingAreaView;

public class CropAndRotateController extends EmptyFilterController {
    private static final int[] ADJUSTABLE_FILTER_PARAMETERS = new int[]{42};
    private BaseFilterButton aspectRatioButton;
    private StaticStyleItemListAdapter aspectRatioListAdapter;
    private CropRotateView cropRotatePreview;
    private float currentAspectRatio = 0.0f;
    private float initialCropAreaBorder;
    private boolean isAspectRatioSwapped = false;
    private ItemSelectorView itemSelectorView;
    private int previewEdgeSpace;
    private int previewSize;

    private class AspectRatioListOnClickListener implements OnClickListener {
        private AspectRatioListOnClickListener() {
        }

        public boolean onItemClick(Integer itemId) {
            if (CropAndRotateController.this.aspectRatioListAdapter.getActiveItemId().equals(itemId)) {
                if (!(itemId.intValue() == 0 || itemId.intValue() == 2)) {
                    CropAndRotateController.this.swapAspectRatio();
                }
            } else if (CropAndRotateController.this.changeParameter(CropAndRotateController.this.getFilterParameter(), 42, itemId.intValue())) {
                TrackerData.getInstance().usingParameter(42, false);
                CropAndRotateController.this.aspectRatioListAdapter.setActiveItemId(itemId);
                CropAndRotateController.this.itemSelectorView.refreshSelectorItems(CropAndRotateController.this.aspectRatioListAdapter, true);
                CropAndRotateController.this.isAspectRatioSwapped = false;
                CropAndRotateController.this.setAspectRatioId(itemId.intValue());
            }
            return true;
        }

        public boolean onContextButtonClick() {
            return false;
        }
    }

    public void init(ControllerContext context) {
        super.init(context);
        WorkingAreaView workingAreaView = getWorkingAreaView();
        workingAreaView.getShadowLayer().setVisibility(4);
        this.cropRotatePreview = new CropRotateView(getContext());
        workingAreaView.addView(this.cropRotatePreview);
        Bitmap image = getTilesProvider().getScreenSourceImage();
        this.cropRotatePreview.setImage(image);
        this.initialCropAreaBorder = ((float) Math.min(image.getWidth(), image.getHeight())) * 0.1f;
        Resources resources = getResources();
        this.previewSize = resources.getDimensionPixelSize(R.dimen.tb_subpanel_preview_size);
        this.previewEdgeSpace = resources.getDimensionPixelSize(R.dimen.tb_subpanel_preview_edge_space);
        setUpStyleDataSource(resources);
        setAspectRatioId(1);
        this.itemSelectorView = getItemSelectorView();
        this.itemSelectorView.reloadSelector(this.aspectRatioListAdapter);
        this.itemSelectorView.setSelectorOnClickListener(new AspectRatioListOnClickListener());
        this.itemSelectorView.setOnVisibilityChangeListener(new OnVisibilityChangeListener() {
            public void onVisibilityChanged(boolean isVisible) {
                CropAndRotateController.this.aspectRatioButton.setSelected(isVisible);
            }
        });
        workingAreaView.getHelpButton().bringToFront();
    }

    private void setUpStyleDataSource(Resources resources) {
        aspectRatioPreviews = new Bitmap[18];
        Bitmap[] stateBitmaps = getRatioItemStateImages(2);
        aspectRatioPreviews[4] = stateBitmaps[0];
        aspectRatioPreviews[5] = stateBitmaps[1];
        stateBitmaps = getRatioItemStateImages(4);
        aspectRatioPreviews[6] = stateBitmaps[0];
        aspectRatioPreviews[7] = stateBitmaps[1];
        stateBitmaps = getRatioItemStateImages(3);
        aspectRatioPreviews[8] = stateBitmaps[0];
        aspectRatioPreviews[9] = stateBitmaps[1];
        stateBitmaps = getRatioItemStateImages(5);
        aspectRatioPreviews[10] = stateBitmaps[0];
        aspectRatioPreviews[11] = stateBitmaps[1];
        stateBitmaps = getRatioItemStateImages(6);
        aspectRatioPreviews[12] = stateBitmaps[0];
        aspectRatioPreviews[13] = stateBitmaps[1];
        stateBitmaps = getRatioItemStateImages(7);
        aspectRatioPreviews[14] = stateBitmaps[0];
        aspectRatioPreviews[15] = stateBitmaps[1];
        stateBitmaps = getRatioItemStateImages(8);
        aspectRatioPreviews[16] = stateBitmaps[0];
        aspectRatioPreviews[17] = stateBitmaps[1];
        this.aspectRatioListAdapter = new StaticStyleItemListAdapter(getFilterParameter(), 42, aspectRatioPreviews);
        this.aspectRatioListAdapter.setActiveItemId(Integer.valueOf(getFilterParameter().getParameterValueOld(42)));
    }

    public void cleanup() {
        super.cleanup();
        WorkingAreaView workingAreaView = getWorkingAreaView();
        workingAreaView.getShadowLayer().setVisibility(0);
        workingAreaView.removeView(this.cropRotatePreview);
    }

    public boolean initLeftFilterButton(BaseFilterButton button) {
        if (button == null) {
            this.aspectRatioButton = null;
            return false;
        }
        this.aspectRatioButton = button;
        this.aspectRatioButton.setStateImages((int) R.drawable.icon_tb_aspect_ratio_default, (int) R.drawable.icon_tb_aspect_ratio_active, 0);
        this.aspectRatioButton.setText(getButtonTitle(R.string.btn_aspect_ratio));
        this.aspectRatioButton.setStyle(R.style.EditToolbarButtonTitle.Selectable);
        this.aspectRatioButton.setBackgroundDrawable(null);
        this.aspectRatioButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CropAndRotateController.this.aspectRatioListAdapter.setActiveItemId(Integer.valueOf(CropAndRotateController.this.getFilterParameter().getParameterValueOld(42)));
                CropAndRotateController.this.itemSelectorView.refreshSelectorItems(CropAndRotateController.this.aspectRatioListAdapter, true);
                CropAndRotateController.this.itemSelectorView.setVisible(true, true);
            }
        });
        return true;
    }

    public BaseFilterButton getLeftFilterButton() {
        return this.aspectRatioButton;
    }

    public int getFilterType() {
        return 20;
    }

    public int[] getGlobalAdjustmentParameters() {
        return ADJUSTABLE_FILTER_PARAMETERS;
    }

    public boolean showsParameterView() {
        return false;
    }

    protected void applyFilter() {
        if (!this._isApplyingFilter) {
            this._isApplyingFilter = true;
            MainActivity.getMainActivity().lockCurrentOrientation();
            new Thread(new Runnable() {
                public void run() {
                    final Bitmap filteredImage = CropAndRotateController.this.applyCropAndRotate(CropAndRotateController.this.cropRotatePreview.getCropAreaSize(), CropAndRotateController.this.cropRotatePreview.getImageTransform());
                    CropAndRotateController.this.getWorkingAreaView().post(new Runnable() {
                        public void run() {
                            MainActivity.getMainActivity().onExportFilteredImage(filteredImage);
                            CropAndRotateController.this._isApplyingFilter = false;
                        }
                    });
                }
            }).start();
        }
    }

    private Bitmap applyCropAndRotate(PointF cropAreaSize, Matrix imageTransform) {
        Bitmap sourceScreen = getTilesProvider().getScreenSourceImage();
        Bitmap result = Bitmap.createBitmap((int) Math.floor((double) cropAreaSize.x), (int) Math.floor((double) cropAreaSize.y), Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setFilterBitmap(true);
        canvas.drawBitmap(sourceScreen, imageTransform, paint);
        return result;
    }

    public void layout(boolean changed, int left, int top, int right, int bottom) {
        if (changed) {
            WorkingAreaView workingAreaView = getWorkingAreaView();
            this.cropRotatePreview.layout(0, 0, workingAreaView.getWidth(), workingAreaView.getHeight() - getEditingToolbar().getHeight());
        }
    }

    private Bitmap[] getRatioItemStateImages(int aspectRatioId) {
        Bitmap[] stateBitmaps = null;
        float ratio = 1.0f;
        Resources resources = getResources();
        switch (aspectRatioId) {
            case 0:
                stateBitmaps = new Bitmap[]{BitmapFactory.decodeResource(resources, R.drawable.icon_fo_free_crop_ratio_default), null};
                break;
            case 1:
                stateBitmaps = new Bitmap[]{BitmapFactory.decodeResource(resources, R.drawable.icon_fo_compare_default), null};
                break;
            case 2:
                ratio = 1.0f;
                break;
            case 3:
                ratio = (float) (1.0d / Math.sqrt(2.0d));
                break;
            case 4:
                ratio = 0.6666667f;
                break;
            case 5:
                ratio = 0.75f;
                break;
            case 6:
                ratio = 0.8f;
                break;
            case 7:
                ratio = 0.71428573f;
                break;
            case 8:
                ratio = 0.5625f;
                break;
        }
        if (stateBitmaps == null) {
            return createAspectRatioStateBitmaps(resources, ratio);
        }
        return stateBitmaps;
    }

    private Bitmap[] createAspectRatioStateBitmaps(Resources resources, float ratio) {
        Paint paint = new Paint();
        paint.setAntiAlias(false);
        paint.setStrokeWidth(1.0f);
        paint.setStyle(Style.STROKE);
        Rect frameRect = new Rect(this.previewEdgeSpace, (this.previewSize - ((int) (((float) (this.previewSize - (this.previewEdgeSpace * 2))) * ratio))) - this.previewEdgeSpace, this.previewSize - (this.previewEdgeSpace * 2), this.previewSize - (this.previewEdgeSpace * 2));
        paint.setColor(resources.getColor(R.color.tb_subpanel_normal_item_title));
        new Canvas(Bitmap.createBitmap(this.previewSize, this.previewSize, Config.ARGB_8888)).drawRect(frameRect, paint);
        paint.setColor(resources.getColor(R.color.tb_subpanel_selected_item_title));
        new Canvas(Bitmap.createBitmap(this.previewSize, this.previewSize, Config.ARGB_8888)).drawRect(frameRect, paint);
        return new Bitmap[]{normalBitmap, selectedBitmap};
    }

    private void setAspectRatioId(int ratioItemId) {
        float aspectRatio = getAspectRatioValue(ratioItemId);
        if (this.isAspectRatioSwapped) {
            aspectRatio = 1.0f / aspectRatio;
        }
        setAspectRatio(aspectRatio);
    }

    private void swapAspectRatio() {
        if (this.currentAspectRatio != 1.0f) {
            this.isAspectRatioSwapped = !this.isAspectRatioSwapped;
            setAspectRatio(1.0f / this.currentAspectRatio);
        }
    }

    private void setAspectRatio(float aspectRatio) {
        this.currentAspectRatio = aspectRatio;
        PointF cropAreaSize = getInitialCropAreaSize(this.currentAspectRatio, this.initialCropAreaBorder);
        this.cropRotatePreview.setCropAreaSize(cropAreaSize.x, cropAreaSize.y, true, false);
    }

    private float getAspectRatioValue(int aspectRatio) {
        switch (aspectRatio) {
            case 1:
                Bitmap image = getTilesProvider().getScreenSourceImage();
                return ((float) image.getHeight()) / ((float) image.getWidth());
            case 3:
                return (float) (1.0d / Math.sqrt(2.0d));
            case 4:
                return 0.6666667f;
            case 5:
                return 0.75f;
            case 6:
                return 0.8f;
            case 7:
                return 0.71428573f;
            case 8:
                return 0.5625f;
            default:
                return 1.0f;
        }
    }

    private PointF getInitialCropAreaSize(float aspectRatio, float border) {
        Bitmap image = getTilesProvider().getScreenSourceImage();
        float maxWidth = ((float) image.getWidth()) - border;
        float maxHeight = ((float) image.getHeight()) - border;
        PointF cropAreaSize = new PointF();
        if (aspectRatio > maxHeight / maxWidth) {
            cropAreaSize.x = maxHeight / aspectRatio;
            cropAreaSize.y = maxHeight;
        } else {
            cropAreaSize.x = maxWidth;
            cropAreaSize.y = maxWidth * aspectRatio;
        }
        return cropAreaSize;
    }
}

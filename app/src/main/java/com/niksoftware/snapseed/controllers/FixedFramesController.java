package com.niksoftware.snapseed.controllers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.controllers.adapters.FrameParamItemListAdapter;
import com.niksoftware.snapseed.controllers.adapters.StyleItemListAdapter;
import com.niksoftware.snapseed.controllers.adapters.StyleItemListAdapter.ActiveItemOverlayProvider;
import com.niksoftware.snapseed.controllers.touchhandlers.TouchHandler;
import com.niksoftware.snapseed.core.DeviceDefs;
import com.niksoftware.snapseed.core.NativeCore;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.SnapseedAppDelegate;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.core.rendering.TilesProvider;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.BaseFilterButton;
import com.niksoftware.snapseed.views.ImageViewGL;
import com.niksoftware.snapseed.views.ItemSelectorView;
import com.niksoftware.snapseed.views.ItemSelectorView.OnClickListener;
import com.niksoftware.snapseed.views.ItemSelectorView.OnVisibilityChangeListener;
import com.niksoftware.snapseed.views.WorkingAreaView;
import java.util.ArrayList;
import java.util.List;

public class FixedFramesController extends AutoshuffleFilterController {
    private NotificationCenterListener _compareModeChangeListener;
    private BaseFilterButton _frameOptionsButton;
    private boolean _frameParamActive;
    private FrameParamItemListAdapter _frameParamAdapter;
    private OnClickListener _frameParamItemClickListener;
    private FrameTouchListener _frameTouchListener;
    private FloatingFrameView _frameView;
    private boolean _isCompareMode;
    private ItemSelectorView _itemSelectorView;
    private int _previewSize;
    private StyleItemListAdapter _styleAdapter;
    private BaseFilterButton _styleButton;
    private OnClickListener _styleItemClickListener;

    private class FloatingFrameView extends View {
        public static final int BORDER_INSET = 5;
        private Drawable _bottomLeftDrawable;
        private Drawable _bottomRightDrawable;
        private int _lastHeight = -1;
        private int _lastWidth = -1;
        private Drawable _topLeftDrawable;
        private Drawable _topRightDrawable;

        public FloatingFrameView(Context context) {
            super(context);
            Resources resources = getResources();
            this._topLeftDrawable = resources.getDrawable(R.drawable.edit_grid_top_left);
            this._topRightDrawable = resources.getDrawable(R.drawable.edit_grid_top_right);
            this._bottomLeftDrawable = resources.getDrawable(R.drawable.edit_grid_bottom_left);
            this._bottomRightDrawable = resources.getDrawable(R.drawable.edit_grid_bottom_right);
        }

        protected void onDraw(Canvas canvas) {
            this._topLeftDrawable.draw(canvas);
            this._topRightDrawable.draw(canvas);
            this._bottomLeftDrawable.draw(canvas);
            this._bottomRightDrawable.draw(canvas);
        }

        public void layout(int left, int top, int right, int bottom) {
            super.layout(left, top, right, bottom);
            int width = getWidth();
            int height = getHeight();
            if (this._lastWidth != width || this._lastHeight != height) {
                int cornerWidth = this._topLeftDrawable.getIntrinsicWidth();
                int cornerHeight = this._topLeftDrawable.getIntrinsicHeight();
                this._topLeftDrawable.setBounds(0, 0, cornerWidth, cornerHeight);
                this._topRightDrawable.setBounds(width - cornerWidth, 0, width, cornerHeight);
                this._bottomLeftDrawable.setBounds(0, height - cornerHeight, cornerWidth, height);
                this._bottomRightDrawable.setBounds(width - cornerWidth, height - cornerHeight, width, height);
                View imageView = FixedFramesController.this.getWorkingAreaView().getImageView();
                FixedFramesController.this._frameTouchListener.setDragAreaSize(new PointF((float) imageView.getWidth(), (float) imageView.getHeight()));
                this._lastWidth = width;
                this._lastHeight = height;
            }
        }
    }

    private class FrameParameterSelectorOnClickListener implements OnClickListener {
        private FrameParameterSelectorOnClickListener() {
        }

        public boolean onItemClick(Integer itemId) {
            FilterParameter filter = FixedFramesController.this.getFilterParameter();
            switch (FixedFramesController.this._frameParamAdapter.getItemParameterType(itemId)) {
                case 9:
                    int i;
                    FixedFramesController fixedFramesController = FixedFramesController.this;
                    if (FixedFramesController.this._frameParamAdapter.isItemActive(itemId)) {
                        i = 0;
                    } else {
                        i = 1;
                    }
                    fixedFramesController.changeParameter(filter, 9, i);
                    TrackerData.getInstance().usingParameter(9, false);
                    break;
                case 224:
                    if (FixedFramesController.this._frameParamAdapter.isItemActive(itemId)) {
                        FixedFramesController.this.changeParameter(filter, 224, 0);
                    } else {
                        FixedFramesController.this.changeParameter(filter, 224, 1);
                    }
                    TrackerData.getInstance().usingParameter(224, false);
                    FixedFramesController.this.updateFrameView(true);
                    break;
            }
            FixedFramesController.this._itemSelectorView.refreshSelectorItems(FixedFramesController.this._frameParamAdapter, true);
            return true;
        }

        public boolean onContextButtonClick() {
            return false;
        }
    }

    private class FrameTouchListener extends TouchHandler {
        private static final float MULTIPLIER = 10000.0f;
        private static final float PINCH_FEEDBACK_RATIO = 2.0f;
        private static final float PINCH_FEEDBACK_THRESHOLD = 2.0f;
        private PointF _dragAreaSize;
        private PointF _dragRangeMaxNorm;
        private PointF _dragRangeMinNorm;
        private PointF _dragStart;
        private float _feedbackRatio = (DeviceDefs.getScreenDensityRatio() * 2.0f);
        private PointF _frameStartNorm;
        private int _pinchStartFrameOffset;
        private float _pinchStartSize;

        public FrameTouchListener(PointF dragAreaSize) {
            setDragAreaSize(dragAreaSize);
        }

        public Rect getFrameRect(int imageWidth, int imageHeight) {
            Rect frameRect;
            FilterParameter filter = FixedFramesController.this.getFilterParameter();
            float offsetNorm;
            if (imageWidth > imageHeight) {
                offsetNorm = ((float) filter.getParameterValueOld(103)) / MULTIPLIER;
                frameRect = new Rect(0, 0, imageHeight, imageHeight);
                frameRect.offset(((imageWidth - imageHeight) / 2) + ((int) Math.ceil((double) (((float) imageWidth) * offsetNorm))), 0);
            } else {
                offsetNorm = ((float) filter.getParameterValueOld(113)) / MULTIPLIER;
                frameRect = new Rect(0, 0, imageWidth, imageWidth);
                frameRect.offset(0, ((imageHeight - imageWidth) / 2) + ((int) Math.ceil((double) (((float) imageHeight) * offsetNorm))));
            }
            if (frameRect.right > imageWidth) {
                frameRect.offset(imageWidth - frameRect.right, 0);
            } else if (frameRect.left < 0) {
                frameRect.offset(-frameRect.left, 0);
            }
            if (frameRect.bottom > imageHeight) {
                frameRect.offset(0, imageHeight - frameRect.bottom);
            } else if (frameRect.top < 0) {
                frameRect.offset(0, -frameRect.top);
            }
            return frameRect;
        }

        public void setDragAreaSize(PointF dragAreaSize) {
            if (this._dragAreaSize == null || !this._dragAreaSize.equals(dragAreaSize)) {
                this._dragAreaSize = dragAreaSize;
                float amp;
                if (this._dragAreaSize.x > this._dragAreaSize.y) {
                    amp = (1.0f - (this._dragAreaSize.y / this._dragAreaSize.x)) / 2.0f;
                    this._dragRangeMinNorm = new PointF(-amp, 0.0f);
                    this._dragRangeMaxNorm = new PointF(amp, 0.0f);
                } else {
                    amp = (1.0f - (this._dragAreaSize.x / this._dragAreaSize.y)) / 2.0f;
                    this._dragRangeMinNorm = new PointF(0.0f, -amp);
                    this._dragRangeMaxNorm = new PointF(0.0f, amp);
                }
                this._dragStart = null;
            }
        }

        public boolean handleTouchDown(float x, float y) {
            FilterParameter filter = FixedFramesController.this.getFilterParameter();
            Rect frameRect = getFrameRect((int) this._dragAreaSize.x, (int) this._dragAreaSize.y);
            if (filter.getParameterValueOld(224) == 0 || !frameRect.contains((int) x, (int) y)) {
                this._dragStart = null;
                return false;
            }
            this._frameStartNorm = new PointF(((float) filter.getParameterValueOld(103)) / MULTIPLIER, ((float) filter.getParameterValueOld(113)) / MULTIPLIER);
            this._dragStart = new PointF(x, y);
            FixedFramesController.this.beginChangeParameter();
            return true;
        }

        public boolean handleTouchMoved(float x, float y) {
            if (this._dragStart == null) {
                return false;
            }
            float dy = (y - this._dragStart.y) / this._dragAreaSize.y;
            float newX = Math.max(Math.min(this._frameStartNorm.x + ((x - this._dragStart.x) / this._dragAreaSize.x), this._dragRangeMaxNorm.x), this._dragRangeMinNorm.x);
            float newY = Math.max(Math.min(this._frameStartNorm.y + dy, this._dragRangeMaxNorm.y), this._dragRangeMinNorm.y);
            FilterParameter filter = FixedFramesController.this.getFilterParameter();
            FixedFramesController.this.changeParameter(filter, 103, Math.round(newX * MULTIPLIER));
            FixedFramesController.this.changeParameter(filter, 113, Math.round(newY * MULTIPLIER));
            FixedFramesController.this.updateFrameView(false);
            FixedFramesController.this.getWorkingAreaView().requestRender();
            return true;
        }

        public void handleTouchUp(float x, float y) {
            FixedFramesController.this.endChangeParameter();
        }

        public void handleTouchCanceled(float x, float y) {
            FixedFramesController.this.endChangeParameter();
        }

        public void handleTouchAbort(boolean pinchBegins) {
            FixedFramesController.this.endChangeParameter();
        }

        public boolean handlePinchBegin(int x, int y, float size, float arc) {
            this._pinchStartSize = size;
            this._pinchStartFrameOffset = FixedFramesController.this.getFilterParameter().getParameterValueOld(221);
            FixedFramesController.this.beginChangeParameter();
            return true;
        }

        public boolean handlePinch(int x, int y, float size, float arc) {
            if (Math.abs(this._pinchStartSize - size) >= 2.0f) {
                FixedFramesController.this.changeParameter(FixedFramesController.this.getFilterParameter(), 221, (int) (((float) this._pinchStartFrameOffset) + ((this._pinchStartSize - size) / this._feedbackRatio)));
            }
            return true;
        }

        public void handlePinchEnd() {
            FixedFramesController.this.endChangeParameter();
        }

        public void handlePinchAbort() {
            FixedFramesController.this.endChangeParameter();
        }
    }

    private class StyleItemOnClickListener implements OnClickListener {
        private StyleItemOnClickListener() {
        }

        public boolean onItemClick(Integer itemId) {
            if (itemId.equals(FixedFramesController.this._styleAdapter.getActiveItemId())) {
                if (NativeCore.frameShouldShuffle(itemId.intValue())) {
                    FixedFramesController.this.randomizeParameter(224);
                }
            } else if (FixedFramesController.this.changeParameter(FixedFramesController.this.getFilterParameter(), 223, itemId.intValue())) {
                TrackerData.getInstance().usingParameter(223, false);
                FixedFramesController.this._styleAdapter.setActiveItemId(itemId);
                FixedFramesController.this._itemSelectorView.refreshSelectorItems(FixedFramesController.this._styleAdapter, true);
            }
            return true;
        }

        public boolean onContextButtonClick() {
            return false;
        }
    }

    public void init(ControllerContext controllerContext) {
        super.init(controllerContext);
        FilterParameter filter = getFilterParameter();
        View imageView = getWorkingAreaView().getImageView();
        TouchHandler frameTouchListener = new FrameTouchListener(new PointF((float) imageView.getWidth(), (float) imageView.getHeight()));
        this._frameTouchListener = frameTouchListener;
        addTouchListener(frameTouchListener);
        Context context = getContext();
        Resources resources = context.getResources();
        this._previewSize = resources.getDimensionPixelSize(R.dimen.tb_subpanel_preview_size);
        final Drawable overlayDrawable = resources.getDrawable(R.drawable.icon_fo_ontop_default);
        final Drawable varOverlayDrawable = resources.getDrawable(R.drawable.icon_fo_ontop_variation_default);
        this._styleAdapter = new StyleItemListAdapter(context, filter, 223, new ActiveItemOverlayProvider() {
            public Drawable getActiveItemOverlayDrawable(Integer itemId) {
                return (itemId == null || !NativeCore.frameShouldShuffle(itemId.intValue())) ? overlayDrawable : varOverlayDrawable;
            }
        }, getTilesProvider().getStyleSourceImage());
        this._styleItemClickListener = new StyleItemOnClickListener();
        this._frameParamAdapter = new FrameParamItemListAdapter(filter);
        this._frameParamItemClickListener = new FrameParameterSelectorOnClickListener();
        createFrameView();
        this._itemSelectorView = getItemSelectorView();
        this._itemSelectorView.setOnVisibilityChangeListener(new OnVisibilityChangeListener() {
            public void onVisibilityChanged(boolean isVisible) {
                if (!isVisible) {
                    FixedFramesController.this._styleButton.setSelected(false);
                    FixedFramesController.this._frameOptionsButton.setSelected(false);
                }
            }
        });
        this._compareModeChangeListener = new NotificationCenterListener() {
            public void performAction(Object isCompareMode) {
                FixedFramesController fixedFramesController = FixedFramesController.this;
                boolean z = isCompareMode != null && ((Boolean) isCompareMode).booleanValue();
                fixedFramesController._isCompareMode = z;
                FixedFramesController.this.updateFrameView(true);
            }
        };
        NotificationCenter.getInstance().addListener(this._compareModeChangeListener, ListenerType.DidChangeCompareImageMode);
    }

    public void cleanup() {
        NotificationCenter.getInstance().removeListener(this._compareModeChangeListener, ListenerType.DidChangeCompareImageMode);
        if (this._frameView != null) {
            getWorkingAreaView().removeView(this._frameView);
            this._frameView = null;
        }
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
        this._styleButton.setStateImages((int) R.drawable.icon_tb_frames_default, (int) R.drawable.icon_tb_frames_active, 0);
        this._styleButton.setText(getButtonTitle(R.string.frame));
        this._styleButton.setStyle(R.style.EditToolbarButtonTitle.Selectable);
        this._styleButton.setBackgroundDrawable(null);
        this._styleButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                FixedFramesController.this._styleButton.setSelected(true);
                FixedFramesController.this._frameParamActive = false;
                FixedFramesController.this._styleAdapter.setActiveItemId(Integer.valueOf(FixedFramesController.this.getFilterParameter().getParameterValueOld(223)));
                FixedFramesController.this._itemSelectorView.refreshSelectorItems(FixedFramesController.this._styleAdapter, true);
                FixedFramesController.this._itemSelectorView.reloadSelector(FixedFramesController.this._styleAdapter, FixedFramesController.this._styleItemClickListener);
                FixedFramesController.this._itemSelectorView.setVisible(true, true);
                FixedFramesController.this.previewImages(223);
            }
        });
        return true;
    }

    public boolean initRightFilterButton(BaseFilterButton button) {
        if (button == null) {
            this._frameOptionsButton = null;
            return false;
        }
        this._frameOptionsButton = button;
        this._frameOptionsButton.setStateImages((int) R.drawable.icon_tb_options_default, (int) R.drawable.icon_tb_options_active, 0);
        this._frameOptionsButton.setText((int) R.string.options);
        this._frameOptionsButton.setStyle(R.style.EditToolbarButtonTitle.Selectable);
        this._frameOptionsButton.setBackgroundDrawable(null);
        this._frameOptionsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FixedFramesController.this._frameOptionsButton.setSelected(true);
                FixedFramesController.this._frameParamActive = true;
                FixedFramesController.this._itemSelectorView.reloadSelector(FixedFramesController.this._frameParamAdapter, FixedFramesController.this._frameParamItemClickListener);
                FixedFramesController.this._itemSelectorView.setVisible(true, true);
            }
        });
        return true;
    }

    public BaseFilterButton getLeftFilterButton() {
        return this._styleButton;
    }

    public BaseFilterButton getRightFilterButton() {
        return this._frameOptionsButton;
    }

    public int getFilterType() {
        return 17;
    }

    public boolean showsParameterView() {
        return false;
    }

    public int[] getGlobalAdjustmentParameters() {
        return new int[0];
    }

    public void setPreviewImage(List<Bitmap> images, int parameter) {
        if (!this._frameParamActive) {
            ArrayList<Bitmap> styleImages = new ArrayList();
            for (Bitmap source : images) {
                int size = Math.min(Math.min(this._previewSize, source.getWidth()), source.getHeight());
                styleImages.add(Bitmap.createBitmap(source, 0, 0, size, size));
            }
            if (this._styleAdapter.updateStylePreviews(styleImages)) {
                this._itemSelectorView.refreshSelectorItems(this._styleAdapter, false);
            }
        }
    }

    protected int getStylePreviewSize() {
        return this._previewSize * 3;
    }

    public void layout(boolean changed, int left, int top, int right, int bottom) {
        super.layout(changed, left, top, right, bottom);
        updateFrameView(false);
    }

    public void onPause() {
        if (this._frameView != null) {
            getWorkingAreaView().removeView(this._frameView);
            this._frameView = null;
        }
        super.onPause();
    }

    public void onResume() {
        super.onResume();
        createFrameView();
        updateFrameView(true);
    }

    public void undoRedoStateChanged() {
        FilterParameter newFilter = getFilterParameter();
        this._styleAdapter.updateFilterParameter(newFilter);
        this._frameParamAdapter.updateFilterParameter(newFilter);
        updateFrameView(true);
        super.undoRedoStateChanged();
    }

    public String getParameterTitle(int parameter) {
        return getContext().getString(R.string.choose_frame);
    }

    public int getHelpResourceId() {
        return R.xml.overlay_fixed_frames;
    }

    private void createFrameView() {
        if (this._frameView == null) {
            this._frameView = new FloatingFrameView(getContext());
            getWorkingAreaView().addView(this._frameView);
            this._frameView.setVisibility(4);
        }
    }

    private void updateFrameView(boolean updateVisibility) {
        if (this._frameView != null) {
            if (updateVisibility) {
                FilterParameter filter = getFilterParameter();
                FloatingFrameView floatingFrameView = this._frameView;
                int i = (this._isCompareMode || filter.getParameterValueOld(224) == 0) ? 4 : 0;
                floatingFrameView.setVisibility(i);
            }
            WorkingAreaView workingAreaView = getWorkingAreaView();
            View imageView = workingAreaView.getImageView();
            View shadowView = workingAreaView.getShadowLayer();
            Rect frameRect = this._frameTouchListener.getFrameRect(imageView.getWidth(), imageView.getHeight());
            frameRect.offset(shadowView.getLeft() + imageView.getLeft(), shadowView.getTop() + imageView.getTop());
            frameRect.inset(-5, -5);
            this._frameView.layout(frameRect.left, frameRect.top, frameRect.right, frameRect.bottom);
        }
    }

    protected void applyFilter() {
        if (!this._isApplyingFilter) {
            this._isApplyingFilter = true;
            lockCurrentOrientation();
            this._frameView.setVisibility(4);
            boolean applyingCrop = false;
            if (getFilterParameter().getParameterValueOld(224) == 1) {
                final TilesProvider tilesProvider = getTilesProvider();
                final Bitmap fullsizeImage = tilesProvider.getSourceImage();
                int imageWidth = fullsizeImage.getWidth();
                int imageHeight = fullsizeImage.getHeight();
                if (imageWidth != imageHeight) {
                    final Rect cropFrame = this._frameTouchListener.getFrameRect(imageWidth, imageHeight);
                    SnapseedAppDelegate.getInstance().progressStart((int) R.string.processing);
                    applyingCrop = true;
                    new Thread(new Runnable() {
                        public void run() {
                            SnapseedAppDelegate.getInstance().progressSetValue(0);
                            Bitmap croppedImage = Bitmap.createBitmap(fullsizeImage, cropFrame.left, cropFrame.top, cropFrame.width(), cropFrame.height());
                            SnapseedAppDelegate.getInstance().progressSetValue(75);
                            tilesProvider.setSourceImage(croppedImage);
                            SnapseedAppDelegate.getInstance().progressSetValue(100);
                            FilterParameter filter = FixedFramesController.this.getFilterParameter();
                            filter.setParameterValueOld(103, 0);
                            filter.setParameterValueOld(113, 0);
                            SnapseedAppDelegate.getInstance().progressEnd();
                            FixedFramesController.this.requestExportImage();
                        }
                    }).start();
                }
            }
            if (!applyingCrop) {
                requestExportImage();
            }
        }
    }

    private void requestExportImage() {
        ((ImageViewGL) getWorkingAreaView().getImageView()).requestRenderImage(getTilesProvider(), null, getFilterParameter(), MainActivity.getMainActivity().createOnRenderExportListener(), false);
    }
}

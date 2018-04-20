package com.niksoftware.snapseed.views;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.R;
import com.niksoftware.snapseed.controllers.FilterController;
import com.niksoftware.snapseed.core.DeviceDefs;
import com.niksoftware.snapseed.core.FilterFactory;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.RenderFilterInterface.RendererLifecycleListener;
import com.niksoftware.snapseed.core.UndoManager;
import com.niksoftware.snapseed.core.UndoObject;
import com.niksoftware.snapseed.core.UndoReceiver;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter.FilterParameterListener;
import com.niksoftware.snapseed.core.rendering.GeometryObject;
import com.niksoftware.snapseed.core.rendering.GeometryObject.Ellipse;
import com.niksoftware.snapseed.core.rendering.TilesProvider;
import java.util.ArrayList;

public final class WorkingAreaView extends RelativeLayout implements UndoReceiver {
    private static final int PHONE_SELECTED_BUTTON_TINT_COLOR = -8421505;
    private static final int REMOVE_GL_VIEW_DELAY = 10;
    private static final int TABLET_SELECTED_BUTTON_TINT_COLOR = -3158065;
    private final ActionView actionView;
    private ActiveParameterView activeParameterView;
    private final int borderWidth;
    private ToolButton compareButton;
    private FilterParameter filterParameter;
    private ImageViewGL glImageView;
    private ToolButton helpButton;
    private ImageView imageView;
    private Rect imageViewScreenRect = new Rect();
    private boolean isCompareMode = false;
    private ParameterView parameterView;
    private Runnable removeGLViewRunnable = null;
    private final ShadowLayer shadowLayer;
    private ImageViewSW swImageView;
    private TilesProvider tilesProvider;
    private Rect visibleFrame = new Rect();

    public WorkingAreaView(Context context) {
        super(context);
        setWillNotDraw(true);
        this.shadowLayer = new ShadowLayer(context);
        addView(this.shadowLayer);
        updateImageViewType(1000);
        this.actionView = new ActionView(context);
        this.actionView.setVisibility(4);
        addView(this.actionView);
        this.borderWidth = getResources().getDimensionPixelSize(R.dimen.wa_border_width);
        addParameterView();
    }

    public void willSetImage(boolean isLoadOrRevert) {
        if (this.swImageView != null) {
            this.swImageView.setImageBitmap(null);
        }
        if (isLoadOrRevert) {
            this.shadowLayer.setVisibility(4);
        }
        if (this.tilesProvider != null) {
            this.tilesProvider.lock();
            this.tilesProvider.cleanup();
            this.tilesProvider.unlock();
            this.tilesProvider = null;
        }
        System.runFinalization();
        System.gc();
    }

    public void setImage(Bitmap bitmap, Bitmap screenImage) {
        if (bitmap.getConfig() != Config.ARGB_8888) {
            throw new IllegalArgumentException("Invalid bitmap pixel format");
        }
        willSetImage(false);
        this.tilesProvider = new TilesProvider(bitmap, screenImage, 1024, false);
        if (this.imageView instanceof ImageViewSW) {
            this.imageView.setImageBitmap(this.tilesProvider.getScreenSourceImage());
        } else if (this.swImageView != null) {
            this.swImageView.setImageBitmap(null);
        }
        this.shadowLayer.setVisibility(0);
        UndoManager.getUndoManager().clear();
        updateShadowBounds();
    }

    public void beginCompare(Bitmap originalScreenImage) {
        if (originalScreenImage != null && (this.imageView instanceof ImageViewSW)) {
            this.isCompareMode = true;
            this.imageView.setImageBitmap(originalScreenImage);
            updateShadowBounds(getFitRect(this.visibleFrame, (float) originalScreenImage.getWidth(), (float) originalScreenImage.getHeight(), (float) getBorder()));
        }
    }

    public void endCompare() {
        if (this.tilesProvider != null && (this.imageView instanceof ImageViewSW)) {
            this.imageView.setImageBitmap(this.tilesProvider.getScreenSourceImage());
            updateShadowBounds();
            this.isCompareMode = false;
        }
    }

    public boolean isComparing() {
        return this.isCompareMode;
    }

    public TilesProvider getTilesProvider() {
        return this.tilesProvider;
    }

    public int getImageWidth() {
        return (this.tilesProvider == null || this.tilesProvider.getSourceImage() == null) ? 1 : this.tilesProvider.getSourceImage().getWidth();
    }

    public int getImageHeight() {
        return (this.tilesProvider == null || this.tilesProvider.getSourceImage() == null) ? 1 : this.tilesProvider.getSourceImage().getHeight();
    }

    public int getBorder() {
        return this.borderWidth;
    }

    public static Rect getFitRect(Rect viewRect, float contentWidth, float contentHeight, float border) {
        float wDraw;
        float hDraw;
        float cellWidth = ((float) viewRect.width()) - (2.0f * border);
        float cellHeight = ((float) viewRect.height()) - (2.0f * border);
        float aspectRatio = contentWidth / contentHeight;
        if (cellWidth / cellHeight < aspectRatio) {
            wDraw = Math.min(cellWidth, contentWidth);
            hDraw = Math.min(cellWidth / aspectRatio, contentHeight);
        } else {
            wDraw = Math.min(cellHeight * aspectRatio, contentWidth);
            hDraw = Math.min(cellHeight, contentHeight);
        }
        float xDraw = (cellWidth - wDraw) / 2.0f;
        float yDraw = (cellHeight - hDraw) / 2.0f;
        return new Rect(viewRect.left + Math.round(xDraw + border), viewRect.top + Math.round(yDraw + border), (viewRect.left + Math.round(wDraw)) + Math.round(xDraw + border), (viewRect.top + Math.round(hDraw)) + Math.round(yDraw + border));
    }

    public Rect getImageViewRect() {
        return getFitRect(this.visibleFrame, (float) getImageWidth(), (float) getImageHeight(), (float) getBorder());
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        doLayout();
    }

    public void doLayout() {
        updateShadowBounds();
        if (this.actionView != null) {
            int top;
            this.actionView.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
            int actionViewWidth = this.actionView.getMeasuredWidth();
            int actionViewHeight = this.actionView.getMeasuredHeight();
            int left = (getWidth() - actionViewWidth) / 2;
            if (this.filterParameter == null || this.filterParameter.getFilterType() == 1) {
                Rect imageRect = getImageViewScreenRect();
                top = imageRect.top + ((imageRect.height() - actionViewHeight) / 2);
            } else {
                top = getResources().getDimensionPixelSize(R.dimen.wa_action_view_top_offset);
            }
            this.actionView.layout(left, top, left + actionViewWidth, top + actionViewHeight);
        }
        forceLayoutForFilterGUI();
        layoutToolButtons();
    }

    private void layoutToolButtons() {
        int margin = getResources().getDimensionPixelSize(R.dimen.tmp_wa_tool_button_margin);
        if (this.helpButton != null) {
            this.helpButton.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
            int buttonWidth = this.helpButton.getMeasuredWidth();
            int buttonHeight = this.helpButton.getMeasuredHeight();
            if (DeviceDefs.isTablet()) {
                this.helpButton.layout((getWidth() - buttonWidth) - margin, margin, getWidth() - margin, buttonHeight + margin);
            } else {
                this.helpButton.layout(margin, margin, buttonWidth + margin, buttonHeight + margin);
            }
        }
        if (this.compareButton != null) {
            this.compareButton.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
            this.compareButton.layout((getWidth() - this.compareButton.getMeasuredWidth()) - margin, margin, getWidth() - margin, this.compareButton.getMeasuredHeight() + margin);
        }
    }

    public void forceLayoutForFilterGUI() {
        if (this.parameterView != null) {
            int left = getLeft();
            int top = getTop();
            int right = getRight();
            int bottom = getBottom();
            this.parameterView.layout(left, top, right, bottom);
            this.activeParameterView.layout(left, top, right, bottom);
        }
        FilterController controller = MainActivity.getMainActivity().getFilterController();
        if (controller != null) {
            Rect rect = getFitRect(this.visibleFrame, (float) getImageWidth(), (float) getImageHeight(), (float) getBorder());
            controller.layout(true, rect.left, rect.top, rect.right, rect.bottom);
        }
    }

    public void setFilterType(int filterType) {
        if (getActiveFilterType() != filterType) {
            UndoManager.getUndoManager().clear();
            NotificationCenter.getInstance().performAction(ListenerType.WillActivateFilter, Integer.valueOf(filterType));
            this.filterParameter = FilterFactory.createFilterParameter(filterType);
            this.filterParameter.addListener(new FilterParameterListener() {
                public void onActiveParameterChanged(int parameterType) {
                    NotificationCenter.getInstance().performAction(ListenerType.DidChangeActiveFilterParameter, Integer.valueOf(parameterType));
                }

                public void onParameterValueChanged(int parameterType, Object value) {
                }
            });
            if (this.helpButton != null && filterType == 1) {
                removeView(this.helpButton);
                this.helpButton = null;
                if (this.compareButton != null) {
                    removeView(this.compareButton);
                    this.compareButton = null;
                }
            } else if (this.helpButton == null && filterType != 1) {
                boolean isTablet = DeviceDefs.isTablet();
                int minSize = getResources().getDimensionPixelSize(R.dimen.tmp_wa_tool_button_min_size);
                LayoutParams layoutParams = new LayoutParams(-2, -2);
                this.helpButton = new ToolButton(getContext());
                this.helpButton.setStateImagesTintColor((int) R.drawable.icon_darkbg_help_default, isTablet ? TABLET_SELECTED_BUTTON_TINT_COLOR : PHONE_SELECTED_BUTTON_TINT_COLOR, 0);
                this.helpButton.setLayoutParams(layoutParams);
                this.helpButton.setMinimumWidth(minSize);
                this.helpButton.setMinimumHeight(minSize);
                addView(this.helpButton);
                if (!isTablet) {
                    this.compareButton = new ToolButton(getContext());
                    this.compareButton.setStateImagesTintColor((int) R.drawable.icon_darkbg_compare_default, (int) PHONE_SELECTED_BUTTON_TINT_COLOR, 0);
                    this.compareButton.setLayoutParams(new LayoutParams(-2, -2));
                    this.compareButton.setMinimumWidth(minSize);
                    this.compareButton.setMinimumHeight(minSize);
                    addView(this.compareButton);
                }
                layoutToolButtons();
            }
            if (filterType == 1) {
                requestRender();
            }
            NotificationCenter.getInstance().performAction(ListenerType.DidActivateFilter, Integer.valueOf(filterType));
        }
    }

    public void updateParameterView(int[] adjustableParameters) {
        if (this.parameterView == null) {
            addParameterView();
        }
        this.parameterView.init(this.filterParameter, adjustableParameters, this.filterParameter.getActiveFilterParameter());
    }

    private void addParameterView() {
        this.parameterView = new ParameterView(getContext());
        this.parameterView.setVisibility(8);
        addView(this.parameterView);
        this.activeParameterView = new ActiveParameterView(getContext());
        this.activeParameterView.setVisibility(8);
        addView(this.activeParameterView);
    }

    public void removeParameterView() {
        if (this.parameterView != null) {
            removeView(this.parameterView);
            this.parameterView = null;
        }
        if (this.activeParameterView != null) {
            removeView(this.activeParameterView);
            this.activeParameterView = null;
        }
    }

    public void addActionView() {
        this.actionView.setHiddenExceptForUndo(false);
    }

    public void removeActionView() {
        this.actionView.setHiddenExceptForUndo(true);
    }

    public int getActiveFilterType() {
        return this.filterParameter == null ? 1000 : this.filterParameter.getFilterType();
    }

    public FilterParameter getFilterParameter() {
        return this.filterParameter;
    }

    public View getImageView() {
        return this.imageView;
    }

    public final Rect getImageViewScreenRect() {
        this.imageViewScreenRect.set(this.shadowLayer.getLeft(), this.shadowLayer.getTop(), this.shadowLayer.getRight(), this.shadowLayer.getBottom());
        this.shadowLayer.shadowToImageRect(this.imageViewScreenRect);
        return this.imageViewScreenRect;
    }

    public void requestRender() {
        if (this.imageView instanceof ImageViewGL) {
            ImageViewGL imageViewGL = this.imageView;
            imageViewGL.setPreviewFilterParameter(getFilterParameter());
            imageViewGL.setPreviewDataSource(getTilesProvider());
            imageViewGL.requestRenderPreview();
        }
    }

    public ParameterView getParameterView() {
        return this.parameterView;
    }

    public ActiveParameterView getActiveParameterView() {
        return this.activeParameterView;
    }

    public ActionView getActionView() {
        return this.actionView;
    }

    public ToolButton getHelpButton() {
        return this.helpButton;
    }

    public ToolButton getCompareButton() {
        return this.compareButton;
    }

    public void makeUndo(UndoObject obj) {
        try {
            this.filterParameter = obj.getFilterParameter().clone();
            String description = obj.getDescription(getContext());
            if (!(this.actionView == null || description == null || description.length() <= 0)) {
                this.actionView.setMessage(description);
            }
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        requestRender();
    }

    public void makeRedo(UndoObject obj) {
        makeUndo(obj);
    }

    public void setGeometryObjects(ArrayList<GeometryObject> geometryObjects) {
        if (this.glImageView != null) {
            this.glImageView.setGeometryObjects(geometryObjects);
        }
    }

    public void clearGeometryObjects() {
        setGeometryObjects(null);
    }

    public void setGeometryObject(GeometryObject geometryObject) {
        ArrayList<GeometryObject> geometryObjects = new ArrayList();
        geometryObjects.add(geometryObject);
        setGeometryObjects(geometryObjects);
    }

    public void setCircle(float x, float y, float radius) {
        setGeometryObject(new Ellipse(x, y, radius, radius, 0.0f));
    }

    public void hideImageView() {
        this.imageView.setVisibility(4);
    }

    public void showImageView() {
        this.imageView.setVisibility(0);
    }

    public void addShadowedView(View v) {
        this.shadowLayer.addView(v);
    }

    public void removeShadowedView(View v) {
        this.shadowLayer.removeView(v);
    }

    public ShadowLayer getShadowLayer() {
        return this.shadowLayer;
    }

    public void updateShadowBounds() {
        updateShadowBounds(getImageViewRect());
    }

    public void updateShadowBounds(Rect imageRect) {
        Rect shadowLayerRect = this.shadowLayer.imageToShadowRect(imageRect);
        this.shadowLayer.layout(shadowLayerRect.left, shadowLayerRect.top, shadowLayerRect.right, shadowLayerRect.bottom);
    }

    public void updateImageViewType(int filterType) {
        boolean requestSWView;
        boolean layout;
        if (filterType == 5 || filterType == 6 || filterType == 1 || filterType == 1000) {
            requestSWView = true;
        } else {
            requestSWView = false;
        }
        if (this.glImageView == null && this.swImageView == null) {
            layout = false;
        } else {
            layout = true;
        }
        if (this.swImageView == null) {
            this.swImageView = new ImageViewSW(getContext());
            this.shadowLayer.addView(this.swImageView);
            this.swImageView.setVisibility(4);
        }
        if (this.glImageView == null) {
            this.glImageView = new ImageViewGL(getContext());
            this.glImageView.setLifecycleListener(new RendererLifecycleListener() {
                public void onRendererInit() {
                    FilterController controller = MainActivity.getMainActivity().getFilterController();
                    if (controller != null) {
                        controller.onOnScreenFilterCreated(controller.getFilterType());
                    }
                }

                public void onRendererCleanUp() {
                }
            });
            this.shadowLayer.addView(this.glImageView, 0);
            this.glImageView.setVisibility(4);
        }
        if (layout) {
            this.shadowLayer.layoutChildren();
        }
        if (this.imageView != null) {
            if ((this.imageView instanceof ImageViewSW) && requestSWView) {
                if (filterType == 5 && this.glImageView != null) {
                    this.glImageView.setVisibility(4);
                    return;
                }
                return;
            } else if ((this.imageView instanceof ImageViewGL) && !requestSWView) {
                return;
            }
        }
        this.imageView = requestSWView ? this.swImageView : this.glImageView;
        if (requestSWView) {
            this.swImageView.setVisibility(0);
            if (this.glImageView != null) {
                this.glImageView.layout(this.glImageView.getLeft(), this.glImageView.getTop(), this.glImageView.getRight(), this.glImageView.getBottom() - 1);
            }
            if (filterType == 5) {
                this.glImageView.setVisibility(4);
            }
        } else {
            this.glImageView.addOnPreviewRenderedListener(MainActivity.getMainActivity().getOnFirstFrameListener(), true);
            this.glImageView.setVisibility(0);
            this.glImageView.resetFirstFrame();
        }
        if (this.tilesProvider != null && this.tilesProvider.getScreenSourceImage() != null && !this.tilesProvider.getScreenSourceImage().isRecycled()) {
            if (requestSWView) {
                this.swImageView.setImageBitmap(this.tilesProvider.getScreenSourceImage());
            } else {
                requestRender();
            }
        }
    }

    public boolean isUsingImageViewGL() {
        return this.imageView == this.glImageView;
    }

    public void activateGLImageViewAnimated() {
        this.swImageView.animate().setDuration(100).alpha(0.0f).setListener(new AnimatorListener() {
            public void onAnimationStart(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
                WorkingAreaView.this.swImageView.setVisibility(4);
                WorkingAreaView.this.swImageView.setAlpha(1.0f);
            }

            public void onAnimationCancel(Animator animator) {
                onAnimationEnd(animator);
            }

            public void onAnimationRepeat(Animator animator) {
            }
        });
    }

    public int getRGBA(int x, int y) {
        return this.tilesProvider.getSourceImage().getPixel(x, y);
    }

    public void setVisualFrame(Rect frameRect) {
        if (!this.visibleFrame.equals(frameRect)) {
            this.visibleFrame = frameRect;
        }
    }

    public Rect getVisualFrame() {
        return this.visibleFrame;
    }

    public void onResume() {
        if (this.removeGLViewRunnable != null) {
            removeCallbacks(this.removeGLViewRunnable);
            this.removeGLViewRunnable.run();
        }
        updateImageViewType(getActiveFilterType());
    }

    public void onPause() {
        if (this.glImageView != null) {
            this.glImageView.setPaused();
            if (this.swImageView != null) {
                this.swImageView.setAlpha(1.0f);
                this.swImageView.setVisibility(0);
                this.swImageView.bringToFront();
            }
            if (this.removeGLViewRunnable == null) {
                this.removeGLViewRunnable = new Runnable() {
                    public void run() {
                        WorkingAreaView.this.removeImageViewGL();
                        WorkingAreaView.this.removeGLViewRunnable = null;
                    }
                };
                postDelayed(this.removeGLViewRunnable, 10);
            }
        }
    }

    public void removeImageViewGL() {
        if (this.glImageView != null) {
            this.shadowLayer.removeView(this.glImageView);
            this.glImageView = null;
            if (this.imageView instanceof ImageViewGL) {
                this.imageView = null;
            }
        }
    }
}

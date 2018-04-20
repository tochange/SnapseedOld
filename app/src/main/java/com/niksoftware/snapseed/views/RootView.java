package com.niksoftware.snapseed.views;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.MainActivity.Screen;
import com.niksoftware.snapseed.R;
import com.niksoftware.snapseed.controllers.FilterController;
import com.niksoftware.snapseed.controllers.StraightenController;
import com.niksoftware.snapseed.controllers.UPointController;
import com.niksoftware.snapseed.core.DeviceDefs;
import com.niksoftware.snapseed.core.FilterDefs.FilterType;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import java.util.ArrayList;
import java.util.Iterator;

public class RootView extends ViewGroup {
    public static final int ANIMATION_TIME = 333;
    private static final boolean PRE_RENDER_BACKGROUND = false;
    private EditingToolBar _editingToolBar;
    private FilterPanelLandscape _filterPanelLandscape;
    private FilterPanelPortrait _filterPanelPortrait;
    private int _first_visible_filter_id = 0;
    private GlobalToolBar _globalToolBar;
    private boolean _hasInit = false;
    boolean _isRunningAnimation;
    private ItemSelectorView _itemSelectorView;
    private ToolButton _revertButtonForNoneTablet;
    private boolean _setScrollPos = false;
    private WorkingAreaView _workingAreaView;
    private final Point displaySizeTemp = new Point();
    private Drawable editScreenBackground;
    public boolean forceLayoutForFilterGUI = false;
    private HelpOverlayView helpOverlay;
    private boolean lastOrientationWasLandscape = false;
    private Drawable mainScreenBackground;
    private final Rect rootViewSizeTemp = new Rect();
    private Drawable transitionScreenBackground;

    public RootView(Context context) {
        super(context);
        init();
    }

    private void init() {
        this._workingAreaView = new WorkingAreaView(getContext());
        this._workingAreaView.setLayoutParams(new LayoutParams(-1, -1));
        addView(this._workingAreaView);
        willEnterSMScreen(Screen.MAIN, false);
        BitmapDrawable background = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), R.drawable.new_bg));
        background.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        background.setAlpha(255);
        background.setAntiAlias(false);
        background.setDither(false);
        background.setFilterBitmap(false);
        this.transitionScreenBackground = background;
        Resources resources = getResources();
        if (resources == null) {
            throw new IllegalStateException("Resources are inaccessible.");
        }
        this.mainScreenBackground = resources.getDrawable(R.drawable.main_screen_background);
        this.editScreenBackground = resources.getDrawable(R.drawable.edit_screen_background);
        setScreenBackground(this.mainScreenBackground);
    }

    private Rect filterPanelBaseFrame(boolean isLandscape, Rect rootViewSize) {
        Resources resources = getResources();
        if (isLandscape) {
            return new Rect(0, getActionBarHeight(), resources.getDimensionPixelSize(R.dimen.tmp_fl_land_panel_width), rootViewSize.height());
        }
        return new Rect(0, 0, rootViewSize.width(), resources.getDimensionPixelSize(R.dimen.tmp_fl_port_panel_height));
    }

    public Rect getWorkingAreaRect(boolean isLandscape, Screen state, Rect rootViewSize) {
        switch (state) {
            case EDIT_CONTROLS_LEFT:
            case EDIT_CONTROLS_RIGHT:
                return new Rect(0, 0, rootViewSize.width(), rootViewSize.height() - (this._editingToolBar == null ? 0 : this._editingToolBar.getHeight()));
            case MAIN:
                Rect filterPanelFrame = filterPanelFrame(isLandscape, state, rootViewSize);
                Rect globalToolBarFrame = globalToolBarFrame(isLandscape, state, rootViewSize);
                if (isLandscape) {
                    return new Rect(filterPanelFrame.width(), getActionBarHeight(), rootViewSize.width(), rootViewSize.height() - globalToolBarFrame.height());
                }
                return new Rect(0, getActionBarHeight(), rootViewSize.width(), ((rootViewSize.height() - globalToolBarFrame.height()) - filterPanelFrame.height()) + getResources().getDimensionPixelSize(R.dimen.fl_gradiend_size));
            default:
                return null;
        }
    }

    public Rect globalToolBarFrame(boolean isLandscape, Screen state, Rect rootViewSize) {
        Rect filterPanelFrame = filterPanelFrame(isLandscape, state, rootViewSize);
        int globalToolBarHeight = 0;
        if (DeviceDefs.isTablet()) {
            this._globalToolBar.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
            globalToolBarHeight = this._globalToolBar.getMeasuredHeight();
        }
        if (isLandscape) {
            return new Rect(filterPanelFrame.width() + 0, (rootViewSize.height() - globalToolBarHeight) - 0, rootViewSize.width() - 0, rootViewSize.height() - 0);
        }
        return new Rect(0, ((rootViewSize.height() - 0) - globalToolBarHeight) - filterPanelFrame.height(), rootViewSize.width() - 0, (rootViewSize.height() - 0) - filterPanelFrame.height());
    }

    public Rect filterPanelFrame(boolean isLandscape, Screen state, Rect rootViewSize) {
        Rect rect = filterPanelBaseFrame(isLandscape, rootViewSize);
        if (!isLandscape) {
            rect.top = rootViewSize.height() - rect.height();
            rect.bottom = rootViewSize.height();
        }
        switch (state) {
            case EDIT_CONTROLS_LEFT:
                rect.left -= rect.width();
                rect.right -= rect.width();
                break;
            case EDIT_CONTROLS_RIGHT:
                rect.left += rootViewSize.width();
                rect.right += rootViewSize.width();
                break;
        }
        return rect;
    }

    public EditingToolBar getEditingToolbar() {
        return this._editingToolBar;
    }

    public void reloadEditingToolbar() {
        if (this._editingToolBar != null) {
            boolean enabled = this._editingToolBar.getEnabled();
            this._editingToolBar.hideUndoPopoverWindow();
            removeView(this._editingToolBar);
            this._editingToolBar = EditingToolBar.createEditingToolbar(getContext());
            FilterController controller = MainActivity.getMainActivity().getFilterController();
            this._editingToolBar.setCompareLabelText(FilterType.isCPUFilter(controller.getFilterType()) ? R.string.preview : R.string.compare_btn);
            addView(this._editingToolBar);
            this._editingToolBar.measure(MeasureSpec.makeMeasureSpec(getWidth(), 1073741824), MeasureSpec.makeMeasureSpec(0, 0));
            this._editingToolBar.layout(0, getHeight() - this._editingToolBar.getMeasuredHeight(), this._editingToolBar.getMeasuredWidth(), getHeight());
            this._editingToolBar.updateFilterController(controller);
            this._editingToolBar.setEnabled(enabled);
            if (controller instanceof UPointController) {
                try {
                    controller.onResume();
                } catch (Exception e) {
                }
            }
        }
    }

    public WorkingAreaView getWorkingAreaView() {
        return this._workingAreaView;
    }

    public GlobalToolBar getGlobalToolBar() {
        return this._globalToolBar;
    }

    public ItemSelectorView getItemSelectorView() {
        return this._itemSelectorView;
    }

    public ArrayList<Animator> willEnterSMScreen(Screen state, boolean animate) {
        int deltaFromX;
        boolean isLandscape = getWidth() > getHeight();
        Rect rootViewSize = new Rect(0, 0, getWidth(), getHeight());
        ArrayList<Animator> animations = animate ? new ArrayList() : null;
        setScreenBackground(this.transitionScreenBackground);
        switch (state) {
            case EDIT_CONTROLS_LEFT:
            case EDIT_CONTROLS_RIGHT:
                setSystemUiVisibility(1);
                if (this._editingToolBar == null) {
                    this._editingToolBar = EditingToolBar.createEditingToolbar(getContext());
                    addView(this._editingToolBar);
                    this._editingToolBar.measure(MeasureSpec.makeMeasureSpec(getWidth(), 1073741824), MeasureSpec.makeMeasureSpec(0, 0));
                    int editingToolbarHeight = this._editingToolBar.getMeasuredHeight();
                    this._editingToolBar.layout(0, getHeight() - this._editingToolBar.getMeasuredHeight(), this._editingToolBar.getMeasuredWidth(), getHeight());
                    if (animate) {
                        animations.add(ObjectAnimator.ofFloat(this._editingToolBar, "y", new float[]{(float) getHeight(), (float) (getHeight() - editingToolbarHeight)}));
                    }
                }
                if (this._itemSelectorView == null) {
                    this._itemSelectorView = new ItemSelectorView(MainActivity.getMainActivity());
                    this._itemSelectorView.measure(MeasureSpec.makeMeasureSpec(getWidth(), 1073741824), MeasureSpec.makeMeasureSpec(getHeight() - this._editingToolBar.getMeasuredHeight(), 1073741824));
                    this._itemSelectorView.setVisibility(8);
                    addView(this._itemSelectorView, indexOfChild(this._editingToolBar));
                    break;
                }
                break;
            case MAIN:
                break;
        }
        View imageView = this._workingAreaView.getImageView();
        if (imageView instanceof ImageViewSW) {
            ((ImageViewSW) imageView).setFillBackground(true);
        }
        Rect rect = filterPanelFrame(isLandscape, state, rootViewSize);
        Screen oldState = MainActivity.getMainActivity().getCurrentScreen();
        if (oldState == Screen.EDIT_CONTROLS_RIGHT) {
            deltaFromX = rootViewSize.right;
        } else if (oldState == Screen.EDIT_CONTROLS_LEFT) {
            deltaFromX = -rect.right;
        } else {
            deltaFromX = -rect.right;
        }
        if (isLandscape) {
            if (this._filterPanelLandscape == null) {
                this._filterPanelLandscape = new FilterPanelLandscape(getContext());
                addView(this._filterPanelLandscape);
                this._setScrollPos = true;
            }
            if (animate && this._filterPanelLandscape != null && state == Screen.MAIN) {
                animations.add(ObjectAnimator.ofFloat(this._filterPanelLandscape, "x", new float[]{(float) deltaFromX, (float) rect.left}));
            } else if (this._filterPanelLandscape != null && state == Screen.MAIN) {
                this._filterPanelLandscape.setupOnClickListeners();
            }
        } else {
            if (this._filterPanelPortrait == null) {
                this._filterPanelPortrait = new FilterPanelPortrait(getContext());
                addView(this._filterPanelPortrait);
                this._setScrollPos = true;
            }
            if (animate && this._filterPanelPortrait != null && state == Screen.MAIN) {
                animations.add(ObjectAnimator.ofFloat(this._filterPanelPortrait, "x", new float[]{(float) deltaFromX, (float) rect.left}));
            } else if (this._filterPanelPortrait != null && state == Screen.MAIN) {
                this._filterPanelPortrait.setupOnClickListeners();
            }
        }
        if (this._globalToolBar == null) {
            this._globalToolBar = new GlobalToolBar(getContext());
            if (DeviceDefs.isTablet()) {
                addView(this._globalToolBar);
            } else {
                View revertButton = this._globalToolBar.getRevertButton();
                this._revertButtonForNoneTablet = revertButton;
                addView(revertButton);
            }
        }
        if (animate && state == Screen.MAIN) {
            if (this._revertButtonForNoneTablet != null) {
                animations.add(ObjectAnimator.ofFloat(this._revertButtonForNoneTablet, "alpha", new float[]{0.0f, 1.0f}));
            } else if (this._globalToolBar != null) {
                animations.add(ObjectAnimator.ofFloat(this._globalToolBar, "alpha", new float[]{0.0f, 1.0f}));
            }
        }
        if (this._hasInit && animate) {
            animateWorkingAreaView(animations, state, isLandscape, rootViewSize);
        }
        return animations;
    }

    private void animateWorkingAreaView(ArrayList<Animator> anims, Screen state, boolean isLandscape, Rect rootViewSize) {
        Rect newWArect = getWorkingAreaRect(isLandscape, state, rootViewSize);
        ShadowLayer shadowLayer = this._workingAreaView.getShadowLayer();
        Rect oldShadowLayerRect = new Rect(shadowLayer.getLeft(), shadowLayer.getTop(), shadowLayer.getRight(), shadowLayer.getBottom());
        Rect newShadowLayerRect = shadowLayer.imageToShadowRect(WorkingAreaView.getFitRect(newWArect, (float) this._workingAreaView.getImageWidth(), (float) this._workingAreaView.getImageHeight(), (float) this._workingAreaView.getBorder()));
        if (MainActivity.getMainActivity()._newFilterType == 5) {
            float scale = StraightenController.imageScaleFactor(newShadowLayerRect.width(), newShadowLayerRect.height());
            int updatedWidth = (int) (((float) newShadowLayerRect.width()) * scale);
            int updatedHeight = (int) (((float) newShadowLayerRect.height()) * scale);
            newShadowLayerRect.left += (newShadowLayerRect.width() - updatedWidth) / 2;
            newShadowLayerRect.top += (newShadowLayerRect.height() - updatedHeight) / 2;
            newShadowLayerRect.right = newShadowLayerRect.left + updatedWidth;
            newShadowLayerRect.bottom = newShadowLayerRect.top + updatedHeight;
        }
        shadowLayer.setPivotX((float) (shadowLayer.getWidth() / 2));
        shadowLayer.setPivotY((float) (shadowLayer.getHeight() / 2));
        shadowLayer.setPivotX(0.0f);
        shadowLayer.setPivotY(0.0f);
        ObjectAnimator transLeft = ObjectAnimator.ofFloat(shadowLayer, "translationX", new float[]{0.0f, (float) (newShadowLayerRect.left - oldShadowLayerRect.left)});
        ObjectAnimator transTop = ObjectAnimator.ofFloat(shadowLayer, "translationY", new float[]{0.0f, (float) (newShadowLayerRect.top - oldShadowLayerRect.top)});
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(shadowLayer, "scaleX", new float[]{1.0f, ((float) newShadowLayerRect.width()) / ((float) oldShadowLayerRect.width())});
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(shadowLayer, "scaleY", new float[]{1.0f, ((float) newShadowLayerRect.height()) / ((float) oldShadowLayerRect.height())});
        anims.add(transLeft);
        anims.add(transTop);
        anims.add(scaleX);
        anims.add(scaleY);
        final Screen screen = state;
        AnimatorListener animListener = new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                RootView.this._isRunningAnimation = true;
                RootView.this._workingAreaView.setEnabled(false);
                RootView.this.setWillNotDraw(true);
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                boolean z;
                RootView.this.setWillNotDraw(false);
                Rect newRootViewSize = new Rect(0, 0, RootView.this.getWidth(), RootView.this.getHeight());
                RootView rootView = RootView.this;
                if (newRootViewSize.width() > newRootViewSize.height()) {
                    z = true;
                } else {
                    z = false;
                }
                Rect rect = rootView.getWorkingAreaRect(z, screen, newRootViewSize);
                RootView.this._workingAreaView.setVisualFrame(rect);
                RootView.this._workingAreaView.getShadowLayer().setScaleX(1.0f);
                RootView.this._workingAreaView.getShadowLayer().setScaleY(1.0f);
                RootView.this._workingAreaView.getShadowLayer().setTranslationX(0.0f);
                RootView.this._workingAreaView.getShadowLayer().setTranslationY(0.0f);
                RootView.this._workingAreaView.getShadowLayer().setPivotX((float) (rect.width() / 2));
                RootView.this._workingAreaView.getShadowLayer().setPivotY((float) (rect.height() / 2));
                RootView.this._workingAreaView.doLayout();
                RootView.this._workingAreaView.setEnabled(true);
                if (screen == Screen.MAIN) {
                    RootView.this._workingAreaView.removeImageViewGL();
                }
                NotificationCenter.getInstance().performAction(screen == Screen.MAIN ? ListenerType.DidEnterMainScreen : ListenerType.DidEnterEditingScreen, null);
                RootView.this.setScreenBackground(screen == Screen.MAIN ? RootView.this.mainScreenBackground : RootView.this.editScreenBackground);
                RootView.this._isRunningAnimation = false;
            }

            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }
        };
        if (state == Screen.MAIN) {
            this._workingAreaView.updateImageViewType(MainActivity.getMainActivity().getFilterController().getFilterType());
        }
        scaleY.addListener(animListener);
    }

    public boolean isRunningAnimation() {
        return this._isRunningAnimation;
    }

    public void didEnterSMScreen(Screen state, boolean animate, ArrayList<Animator> existingAnimations) {
        if (animate) {
            boolean updateImageviewType = false;
            ArrayList<Animator> animations = existingAnimations != null ? existingAnimations : new ArrayList();
            ObjectAnimator animator;
            switch (state) {
                case EDIT_CONTROLS_LEFT:
                case EDIT_CONTROLS_RIGHT:
                    final View panel = this._filterPanelLandscape != null ? this._filterPanelLandscape : this._filterPanelPortrait;
                    this._first_visible_filter_id = ((FilterPanelInterface) panel).getFirstVisibleFilterId();
                    animator = ObjectAnimator.ofFloat(panel, "x", new float[]{(float) panel.getLeft(), (float) (panel.getLeft() - panel.getWidth())});
                    animations.add(animator);
                    this._filterPanelLandscape = null;
                    this._filterPanelPortrait = null;
                    animator.addListener(new AnimatorListener() {
                        public void onAnimationStart(Animator animation) {
                            RootView.this._isRunningAnimation = true;
                        }

                        public void onAnimationRepeat(Animator animation) {
                        }

                        public void onAnimationEnd(Animator animation) {
                            RootView.this.removeView(panel);
                        }

                        public void onAnimationCancel(Animator animation) {
                            onAnimationEnd(animation);
                        }
                    });
                    updateImageviewType = true;
                    if (this._globalToolBar != null) {
                        if (DeviceDefs.isTablet()) {
                            removeView(this._globalToolBar);
                        } else if (this._revertButtonForNoneTablet != null) {
                            removeView(this._revertButtonForNoneTablet);
                            this._revertButtonForNoneTablet = null;
                        }
                        this._globalToolBar = null;
                        updateImageviewType = true;
                        break;
                    }
                    break;
                case MAIN:
                    if (this._editingToolBar != null) {
                        animator = ObjectAnimator.ofFloat(this._editingToolBar, "y", new float[]{(float) this._editingToolBar.getTop(), (float) (this._editingToolBar.getTop() + this._editingToolBar.getHeight())});
                        animations.add(animator);
                        final EditingToolBar copy = this._editingToolBar;
                        this._editingToolBar = null;
                        animator.addListener(new AnimatorListener() {
                            public void onAnimationStart(Animator animation) {
                                RootView.this._isRunningAnimation = true;
                            }

                            public void onAnimationRepeat(Animator animation) {
                            }

                            public void onAnimationEnd(Animator animation) {
                                MainActivity.getMainActivity().unlockCurrentOrientation();
                                RootView.this.removeView(copy);
                                copy.cleanup();
                                RootView.this.hideHelpOverlay(false);
                                RootView.this._isRunningAnimation = false;
                                if (RootView.this._filterPanelLandscape != null) {
                                    RootView.this._filterPanelLandscape.setupOnClickListeners();
                                }
                                if (RootView.this._filterPanelPortrait != null) {
                                    RootView.this._filterPanelPortrait.setupOnClickListeners();
                                }
                            }

                            public void onAnimationCancel(Animator animation) {
                                onAnimationEnd(animation);
                            }
                        });
                    }
                    if (this._itemSelectorView != null) {
                        removeView(this._itemSelectorView);
                        this._itemSelectorView = null;
                        break;
                    }
                    break;
            }
            if (!animations.isEmpty()) {
                final ArrayList<AnimatorListener> listeners = new ArrayList();
                Iterator i$ = animations.iterator();
                while (i$.hasNext()) {
                    Animator animator2 = (Animator) i$.next();
                    if (animator2.getListeners() != null) {
                        listeners.addAll(animator2.getListeners());
                        animator2.removeAllListeners();
                    }
                }
                if (updateImageviewType) {
                    listeners.add(new AnimatorListener() {
                        public void onAnimationStart(Animator animation) {
                        }

                        public void onAnimationRepeat(Animator animation) {
                        }

                        public void onAnimationEnd(Animator animation) {
                            RootView.this._workingAreaView.updateImageViewType(MainActivity.getMainActivity().getFilterController().getFilterType());
                            RootView.this._workingAreaView.requestLayout();
                        }

                        public void onAnimationCancel(Animator animation) {
                            onAnimationEnd(animation);
                        }
                    });
                }
                AnimatorSet set = new AnimatorSet();
                set.playTogether(animations);
                set.setDuration(333);
                set.setInterpolator(new AccelerateDecelerateInterpolator());
                if (listeners.size() > 0) {
                    set.addListener(new AnimatorListener() {
                        public void onAnimationStart(Animator animation) {
                            Iterator i$ = listeners.iterator();
                            while (i$.hasNext()) {
                                ((AnimatorListener) i$.next()).onAnimationStart(animation);
                            }
                        }

                        public void onAnimationRepeat(Animator animation) {
                            Iterator i$ = listeners.iterator();
                            while (i$.hasNext()) {
                                ((AnimatorListener) i$.next()).onAnimationRepeat(animation);
                            }
                        }

                        public void onAnimationEnd(Animator animation) {
                            RootView.this.post(new Runnable() {
                                public void run() {
                                    AnonymousClass5.this.onAnimationEndDelayed();
                                }
                            });
                        }

                        public void onAnimationCancel(Animator animation) {
                            onAnimationEnd(animation);
                        }

                        private void onAnimationEndDelayed() {
                            Iterator i$ = listeners.iterator();
                            while (i$.hasNext()) {
                                ((AnimatorListener) i$.next()).onAnimationEnd(null);
                            }
                        }
                    });
                }
                set.start();
            }
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this._filterPanelLandscape != null) {
            this._filterPanelLandscape.measure(widthMeasureSpec, heightMeasureSpec);
        }
        if (this._filterPanelPortrait != null) {
            this._filterPanelPortrait.measure(widthMeasureSpec, heightMeasureSpec);
        }
        if (this._editingToolBar != null) {
            this._editingToolBar.measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        boolean isLandscape = right - left > bottom - top;
        Screen state = MainActivity.getMainActivity().getCurrentScreen();
        this.rootViewSizeTemp.set(0, 0, getWidth(), getHeight());
        if (this._workingAreaView != null) {
            this._workingAreaView.setVisualFrame(getWorkingAreaRect(isLandscape, state, this.rootViewSizeTemp));
            this._workingAreaView.layout(0, 0, getWidth(), getHeight());
            if (this.forceLayoutForFilterGUI) {
                this._workingAreaView.forceLayoutForFilterGUI();
                this.forceLayoutForFilterGUI = false;
            }
            this._hasInit = true;
        }
        MainActivity.getMainActivity().getWindowManager().getDefaultDisplay().getSize(this.displaySizeTemp);
        if (!(this._filterPanelLandscape == null && this._filterPanelPortrait == null)) {
            if (this.displaySizeTemp.y > this.displaySizeTemp.x) {
                if (this._filterPanelLandscape != null) {
                    this._first_visible_filter_id = this._filterPanelLandscape.getFirstVisibleFilterId();
                    removeView(this._filterPanelLandscape);
                    this._filterPanelLandscape = null;
                }
                if (this._filterPanelPortrait == null) {
                    this._filterPanelPortrait = new FilterPanelPortrait(getContext());
                    addView(this._filterPanelPortrait);
                    this._filterPanelPortrait.setupOnClickListeners();
                }
            } else {
                if (this._filterPanelPortrait != null) {
                    this._first_visible_filter_id = this._filterPanelPortrait.getFirstVisibleFilterId();
                    removeView(this._filterPanelPortrait);
                    this._filterPanelPortrait = null;
                }
                if (this._filterPanelLandscape == null) {
                    this._filterPanelLandscape = new FilterPanelLandscape(getContext());
                    addView(this._filterPanelLandscape);
                    this._filterPanelLandscape.setupOnClickListeners();
                }
            }
        }
        Rect rect = filterPanelFrame(isLandscape, state, this.rootViewSizeTemp);
        if (this._filterPanelLandscape != null) {
            this._filterPanelLandscape.layout(rect.left, rect.top, rect.right, rect.bottom);
            if (this.lastOrientationWasLandscape != isLandscape || this._setScrollPos) {
                this._filterPanelLandscape.setFirstVisibleFilterId(this._first_visible_filter_id);
            }
            this._setScrollPos = false;
        }
        if (this._filterPanelPortrait != null) {
            this._filterPanelPortrait.layout(rect.left, rect.top, rect.right, rect.bottom);
            if (this.lastOrientationWasLandscape != isLandscape || this._setScrollPos) {
                this._filterPanelPortrait.setFirstVisibleFilterId(this._first_visible_filter_id);
            }
            this._setScrollPos = false;
        }
        if (this._globalToolBar != null) {
            int width;
            int height;
            if (DeviceDefs.isTablet()) {
                rect = globalToolBarFrame(isLandscape, state, this.rootViewSizeTemp);
                this._globalToolBar.measure(0, 0);
                width = this._globalToolBar.getMeasuredWidth();
                height = this._globalToolBar.getMeasuredHeight();
                this._globalToolBar.layout(((rect.right + rect.left) - width) / 2, ((rect.bottom + rect.top) - height) / 2, ((rect.right + rect.left) + width) / 2, ((rect.bottom + rect.top) + height) / 2);
            } else if (this._revertButtonForNoneTablet != null) {
                rect = getWorkingAreaRect(isLandscape, state, this.rootViewSizeTemp);
                this._revertButtonForNoneTablet.measure(0, 0);
                width = this._revertButtonForNoneTablet.getMeasuredWidth();
                height = this._revertButtonForNoneTablet.getMeasuredHeight();
                int margin = getResources().getDimensionPixelSize(R.dimen.tmp_wa_tool_button_margin);
                this._revertButtonForNoneTablet.layout(rect.left + margin, rect.top + margin, (rect.left + width) + margin, (rect.top + height) + margin);
            }
        }
        int editingToolbarHeight = 0;
        if (this._editingToolBar != null) {
            if (!(this._editingToolBar.getTranslationY() == 0.0f || isRunningAnimation())) {
                this._editingToolBar.setTranslationY(0.0f);
            }
            this._editingToolBar.measure(MeasureSpec.makeMeasureSpec(getWidth(), 1073741824), MeasureSpec.makeMeasureSpec(0, 0));
            int editingToolbarWidth = this._editingToolBar.getMeasuredWidth();
            editingToolbarHeight = this._editingToolBar.getMeasuredHeight();
            int editingToolbarTop = getHeight() - editingToolbarHeight;
            this._editingToolBar.layout(0, editingToolbarTop, editingToolbarWidth, editingToolbarTop + editingToolbarHeight);
        }
        if (this._itemSelectorView != null) {
            this._itemSelectorView.measure(MeasureSpec.makeMeasureSpec(getWidth(), 1073741824), MeasureSpec.makeMeasureSpec(getHeight() - editingToolbarHeight, 1073741824));
            this._itemSelectorView.layout(0, (getHeight() - editingToolbarHeight) - this._itemSelectorView.getMeasuredHeight(), this._itemSelectorView.getMeasuredWidth(), getHeight() - editingToolbarHeight);
        }
        if (this.helpOverlay != null) {
            this.helpOverlay.layout(0, state == Screen.MAIN ? getActionBarHeight() : 0, getWidth(), getHeight());
        }
        this.lastOrientationWasLandscape = isLandscape;
    }

    private boolean isTouchOutItemSelector(MotionEvent event) {
        return event.getAction() == 0 && this._itemSelectorView != null && this._itemSelectorView.getVisibility() == 0 && (((int) event.getY()) > this._itemSelectorView.getBottom() || ((int) event.getY()) < this._itemSelectorView.getTop());
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        Screen state = MainActivity.getMainActivity().getCurrentScreen();
        if (state != Screen.MAIN && isTouchOutItemSelector(event)) {
            if (this._itemSelectorView != null) {
                this._itemSelectorView.setVisible(false, true);
            }
            return true;
        } else if (super.dispatchTouchEvent(event)) {
            return true;
        } else {
            if (DeviceDefs.isTablet() || state != Screen.MAIN) {
                return false;
            }
            boolean imageViewHit = this._workingAreaView.getImageViewScreenRect().contains((int) event.getX(), (int) event.getY());
            int action = event.getAction();
            if (this._workingAreaView.isComparing()) {
                if (imageViewHit && action != 3 && action != 1) {
                    return false;
                }
                this._workingAreaView.endCompare();
                return true;
            } else if (!imageViewHit || action != 0) {
                return false;
            } else {
                this._workingAreaView.beginCompare(MainActivity.getMainActivity().getEditSession().getOriginalScreenImage());
                return true;
            }
        }
    }

    public View getFilterList() {
        return this._filterPanelLandscape != null ? this._filterPanelLandscape : this._filterPanelPortrait;
    }

    public void showHelpOverlay(int overlayXmlResId) {
        HelpOverlayView helpOverlay = new HelpOverlayView(getContext());
        helpOverlay.setUp(overlayXmlResId, getWidth(), getHeight());
        if (this.helpOverlay != null) {
            removeView(this.helpOverlay);
            this.helpOverlay = null;
        }
        this.helpOverlay = helpOverlay;
        addView(this.helpOverlay);
        helpOverlay.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == 0) {
                    RootView.this.hideHelpOverlay(true);
                }
                return true;
            }
        });
        bringToFront();
    }

    public void hideHelpOverlay(boolean animated) {
        if (this.helpOverlay != null) {
            if (animated) {
                this.helpOverlay.animate().alpha(0.0f).setListener(new AnimatorListener() {
                    public void onAnimationStart(Animator animator) {
                    }

                    public void onAnimationEnd(Animator animator) {
                        RootView.this.removeView(RootView.this.helpOverlay);
                        RootView.this.helpOverlay = null;
                    }

                    public void onAnimationCancel(Animator animator) {
                        onAnimationEnd(animator);
                    }

                    public void onAnimationRepeat(Animator animator) {
                    }
                });
                return;
            }
            removeView(this.helpOverlay);
            this.helpOverlay = null;
        }
    }

    public void closeAllPopovers() {
        if (this._itemSelectorView != null) {
            this._itemSelectorView.setVisible(false, false);
        }
        if (this._editingToolBar != null) {
            this._editingToolBar.hideUndoPopoverWindow();
            this._editingToolBar.resetStyleButtons();
        }
    }

    private int getActionBarHeight() {
        return getResources().getDimensionPixelSize(R.dimen.action_bar_height);
    }

    private void setScreenBackground(Drawable drawable) {
        int width = getWidth();
        int height = getHeight();
        setBackgroundDrawable(drawable);
    }
}

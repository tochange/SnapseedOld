package com.niksoftware.snapseed.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.ConditionVariable;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.niksoftware.snapseed.R;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

public class ItemSelectorView extends RelativeLayout {
    private static final int PUSH_POP_ANIMATION_DELAY = 333;
    private Animation _inAnimation;
    private ConditionVariable _inTransition = new ConditionVariable(true);
    private ItemSelectorPanel _itemSelectorPanel;
    private Animation _outAnimation;
    private RelativeLayout _panelContainer;
    private Stack<ItemSelectorPanel> _preservedStates = new Stack();
    private OnVisibilityChangeListener _visibilityListener;

    public interface OnVisibilityChangeListener {
        void onVisibilityChanged(boolean z);
    }

    public interface OnClickListener {
        boolean onContextButtonClick();

        boolean onItemClick(Integer num);
    }

    public interface ListAdapter {
        int getContextButtonImageId();

        String getContextButtonText(Context context);

        int getItemCount();

        Integer getItemId(int i);

        Object[] getItemStateImages(Context context, Integer num);

        String getItemText(Context context, Integer num);

        boolean hasContextItem();

        boolean isItemActive(Integer num);
    }

    private static class ItemSelectorPanel extends RelativeLayout {
        private ToolButton _contextButton;
        private LinearLayout _itemContainer;
        private HorizontalScrollView _itemScrollContainer;
        private ArrayList<ToolButton> _itemViews = new ArrayList();
        private OnClickListener _onClickListener;
        private int _panelItemSpacing;
        private View _separatorView;

        public ItemSelectorPanel(Context context) {
            super(context);
            Resources resources = getResources();
            this._panelItemSpacing = resources.getDimensionPixelSize(R.dimen.tb_subpanel_item_spacing);
            this._itemScrollContainer = new HorizontalScrollView(context);
            this._itemScrollContainer.setId(1);
            LayoutParams scrollPanelLayoutParameters = new LayoutParams(-1, -2);
            scrollPanelLayoutParameters.addRule(10, -1);
            scrollPanelLayoutParameters.addRule(9, -1);
            scrollPanelLayoutParameters.addRule(0, 2);
            addView(this._itemScrollContainer, scrollPanelLayoutParameters);
            this._separatorView = new SeparatorView(context);
            this._separatorView.setId(2);
            LayoutParams separatorLayoutParameters = new LayoutParams(2, -2);
            separatorLayoutParameters.addRule(6, 1);
            separatorLayoutParameters.addRule(8, 1);
            separatorLayoutParameters.addRule(0, 3);
            addView(this._separatorView, separatorLayoutParameters);
            this._contextButton = new ToolButton(context);
            this._contextButton.setId(3);
            this._contextButton.setTitleVisible(true);
            this._contextButton.setStyleNoShadow(R.style.ParameterSelectorItem);
            this._contextButton.setPadding(this._panelItemSpacing, this._panelItemSpacing, this._panelItemSpacing, this._panelItemSpacing);
            this._contextButton.setMinimumWidth(resources.getDimensionPixelSize(R.dimen.tb_subpanel_context_button_width));
            LayoutParams shuffleButtonLayoutParams = new LayoutParams(-2, -2);
            shuffleButtonLayoutParams.addRule(11, -1);
            shuffleButtonLayoutParams.addRule(8, 1);
            addView(this._contextButton, shuffleButtonLayoutParams);
            this._itemContainer = new LinearLayout(context);
            this._itemContainer.setPadding(this._panelItemSpacing / 2, 0, this._panelItemSpacing / 2, 0);
            this._itemScrollContainer.addView(this._itemContainer, new FrameLayout.LayoutParams(-1, -2));
            this._contextButton.setOnClickListener(new android.view.View.OnClickListener() {
                public void onClick(View view) {
                    if (ItemSelectorPanel.this._onClickListener != null) {
//                        ItemSelectorPanel.this._onClickListener.onContextButtonClick();
                    }
                }
            });
        }

        public void reloadSelector(ListAdapter adapter) {
            this._itemContainer.removeAllViews();
            this._itemViews = createSelectorButtons(adapter);
            if (adapter == null || !adapter.hasContextItem()) {
                this._separatorView.setVisibility(8);
                this._contextButton.setVisibility(8);
                return;
            }
            this._separatorView.setVisibility(0);
            this._contextButton.setStateImagesTintColor(adapter.getContextButtonImageId(), -8421505, 0);
            this._contextButton.setText(adapter.getContextButtonText(getContext()));
            this._contextButton.setVisibility(0);
        }

        public boolean refreshSelectorItems(ListAdapter adapter, boolean updateStateOnly) {
            if (this._itemViews == null || adapter == null) {
                return false;
            }
            Iterator i$ = this._itemViews.iterator();
            while (i$.hasNext()) {
                ToolButton item = (ToolButton) i$.next();
                Integer itemId = (Integer) item.getTag();
                if (!updateStateOnly) {
                    Context context = getContext();
                    updateItemStateImages(item, adapter.getItemStateImages(context, itemId));
                    item.setText(adapter.getItemText(context, itemId));
                }
                item.setSelected(adapter.isItemActive(itemId));
            }
            return true;
        }

        public void setSelectorOnClickListener(OnClickListener onClickListener) {
            this._onClickListener = onClickListener;
        }

        private ArrayList<ToolButton> createSelectorButtons(ListAdapter adapter) {
            if (adapter == null) {
                return null;
            }
            Context context = getContext();
            int itemCount = adapter.getItemCount();
            ArrayList<ToolButton> selectorButtons = new ArrayList(itemCount);
            for (int i = 0; i < itemCount; i++) {
                View itemView;
                LinearLayout.LayoutParams layoutParams;
                Integer itemId = adapter.getItemId(i);
                if (itemId != null) {
                    View item = new ToolButton(context);
                    updateItemStateImages(item, adapter.getItemStateImages(context, itemId));
                    item.setText(adapter.getItemText(context, itemId));
                    item.setTitleVisible(true);
                    item.setStyleNoShadow(R.style.ParameterSelectorItem);
                    item.setTag(itemId);
                    item.setSelected(adapter.isItemActive(itemId));
                    item.setOnClickListener(new android.view.View.OnClickListener() {
                        public void onClick(View v) {
                            if (ItemSelectorPanel.this._onClickListener != null) {
                                ItemSelectorPanel.this._onClickListener.onItemClick((Integer) v.getTag());
                            }
                        }
                    });
                    selectorButtons.add(item);
                    itemView = item;
                    layoutParams = new LinearLayout.LayoutParams(-2, -2);
                    layoutParams.gravity = 80;
                    itemView.setPadding(this._panelItemSpacing / 2, this._panelItemSpacing, this._panelItemSpacing / 2, this._panelItemSpacing);
                } else {
                    layoutParams = new LinearLayout.LayoutParams(2, -1);
                    itemView = new SeparatorView(getContext());
                }
                this._itemContainer.addView(itemView, layoutParams);
            }
            return selectorButtons;
        }

        private void updateItemStateImages(ToolButton item, Object[] stateImages) {
            if (stateImages != null && stateImages.length >= 1) {
                if (stateImages instanceof Bitmap[]) {
                    Bitmap activeState;
                    Bitmap normalState = stateImages[0];
                    if (stateImages.length > 1) {
                        activeState = (Bitmap) stateImages[1];
                    } else {
                        activeState = normalState;
                    }
                    item.setStateImages(normalState, activeState, null);
                } else if (stateImages instanceof Integer[]) {
                    int activeStateId;
                    int normalStateId = ((Integer) stateImages[0]).intValue();
                    if (stateImages.length > 1) {
                        activeStateId = ((Integer) stateImages[1]).intValue();
                    } else {
                        activeStateId = normalStateId;
                    }
                    item.setStateImages(normalStateId, activeStateId, 0);
                }
            }
        }

        ToolButton getContextButton() {
            return this._contextButton;
        }
    }

    private static class SeparatorView extends View {
        public static final int SEPARATOR_WIDTH = 2;

        public SeparatorView(Context context) {
            super(context);
        }

        protected void onDraw(Canvas canvas) {
            Paint paint = new Paint();
            paint.setColor(-1773121456);
            int height = getHeight();
            paint.setStrokeWidth(1.0f);
            canvas.drawLine(1.0f, 0.0f, 1.0f, (float) height, paint);
            paint.setColor(-16777216);
            canvas.drawLine(0.0f, 1.0f, 0.0f, (float) height, paint);
        }
    }

    public ItemSelectorView(Context context) {
        super(context);
        this._panelContainer = new RelativeLayout(context);
        this._panelContainer.setPadding(0, getResources().getDimensionPixelSize(R.dimen.tb_subpanel_shadow_height) + 1, 0, 0);
        LayoutParams containerLayoutParameters = new LayoutParams(-1, -2);
        containerLayoutParameters.addRule(12, -1);
        addView(this._panelContainer, containerLayoutParameters);
        setOnClickListener(new android.view.View.OnClickListener() {
            public void onClick(View view) {
                if (view instanceof ItemSelectorView) {
                    ItemSelectorView.this.setVisible(false, true);
                }
            }
        });
    }

    public void reloadSelector(ListAdapter adapter, OnClickListener onClickListener) {
        if (this._itemSelectorPanel == null) {
            this._itemSelectorPanel = new ItemSelectorPanel(getContext());
            this._panelContainer.addView(this._itemSelectorPanel);
        }
        this._itemSelectorPanel.reloadSelector(adapter);
        this._itemSelectorPanel.setSelectorOnClickListener(onClickListener);
        this._panelContainer.requestLayout();
    }

    public void reloadSelector(ListAdapter adapter) {
        reloadSelector(adapter, null);
    }

    public boolean refreshSelectorItems(ListAdapter adapter, boolean updateStateOnly) {
        return this._itemSelectorPanel != null && this._itemSelectorPanel.refreshSelectorItems(adapter, updateStateOnly);
    }

    public synchronized boolean pushSelectorState(ListAdapter adapter) {
        boolean z = false;
        synchronized (this) {
            if (this._itemSelectorPanel != null && this._inTransition.block(1)) {
                setEnabled(false);
                this._inTransition.close();
                this._outAnimation = new TranslateAnimation(2, 0.0f, 2, 0.0f, 1, 0.0f, 1, -1.0f);
                this._outAnimation.setDuration(333);
                this._preservedStates.push(this._itemSelectorPanel);
                final View oldPanel = this._itemSelectorPanel;
                this._outAnimation.setAnimationListener(new AnimationListener() {
                    public void onAnimationStart(Animation animation) {
                    }

                    public void onAnimationEnd(Animation animation) {
                        ItemSelectorView.this._outAnimation = null;
                        oldPanel.setVisibility(8);
                        ItemSelectorView.this.setEnabled(true);
                        ItemSelectorView.this._inTransition.open();
                    }

                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                this._inAnimation = new TranslateAnimation(2, 0.0f, 2, 0.0f, 1, 1.0f, 1, 0.0f);
                this._inAnimation.setDuration(333);
                this._inAnimation.setAnimationListener(new AnimationListener() {
                    public void onAnimationStart(Animation animation) {
                    }

                    public void onAnimationEnd(Animation animation) {
                        ItemSelectorView.this._inAnimation = null;
                    }

                    public void onAnimationRepeat(Animation animation) {
                    }
                });
                this._itemSelectorPanel = new ItemSelectorPanel(getContext());
                this._itemSelectorPanel.reloadSelector(adapter);
                this._panelContainer.addView(this._itemSelectorPanel);
                this._itemSelectorPanel.startAnimation(this._inAnimation);
                oldPanel.startAnimation(this._outAnimation);
                z = true;
            }
        }
        return z;
    }

    public synchronized boolean popSelectorState(boolean animated) {
        boolean z = false;
        synchronized (this) {
            if (!this._preservedStates.empty() && this._inTransition.block(1)) {
                setEnabled(false);
                this._inTransition.close();
                if (animated) {
                    this._outAnimation = new TranslateAnimation(2, 0.0f, 2, 0.0f, 1, 0.0f, 1, 1.0f);
                    this._outAnimation.setDuration(333);
                    final View oldPanel = this._itemSelectorPanel;
                    this._itemSelectorPanel.setSelectorOnClickListener(null);
                    this._outAnimation.setAnimationListener(new AnimationListener() {
                        public void onAnimationStart(Animation animation) {
                        }

                        public void onAnimationEnd(Animation animation) {
                            ItemSelectorView.this._outAnimation = null;
                            ItemSelectorView.this._panelContainer.removeView(oldPanel);
                            ItemSelectorView.this.setEnabled(true);
                            ItemSelectorView.this._inTransition.open();
                        }

                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    this._inAnimation = new TranslateAnimation(2, 0.0f, 2, 0.0f, 1, -1.0f, 1, 0.0f);
                    this._inAnimation.setDuration(333);
                    this._inAnimation.setAnimationListener(new AnimationListener() {
                        public void onAnimationStart(Animation animation) {
                        }

                        public void onAnimationEnd(Animation animation) {
                            ItemSelectorView.this._inAnimation = null;
                        }

                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    this._itemSelectorPanel = (ItemSelectorPanel) this._preservedStates.pop();
                    this._itemSelectorPanel.setVisibility(0);
                    this._itemSelectorPanel.startAnimation(this._inAnimation);
                    oldPanel.startAnimation(this._outAnimation);
                } else {
                    this._itemSelectorPanel.setSelectorOnClickListener(null);
                    this._panelContainer.removeView(this._itemSelectorPanel);
                    this._itemSelectorPanel = (ItemSelectorPanel) this._preservedStates.pop();
                    this._itemSelectorPanel.setVisibility(0);
                    this._inTransition.open();
                    setEnabled(true);
                }
                z = true;
            }
        }
        return z;
    }

    public void setSelectorOnClickListener(OnClickListener onClickListener) {
        if (this._itemSelectorPanel != null) {
            this._itemSelectorPanel.setSelectorOnClickListener(onClickListener);
        }
    }

    public void setOnVisibilityChangeListener(OnVisibilityChangeListener listener) {
        this._visibilityListener = listener;
    }

    public void cleanup() {
        cancelAnimations();
        setOnVisibilityChangeListener(null);
        setSelectorOnClickListener(null);
        while (!this._preservedStates.empty()) {
            ((ItemSelectorPanel) this._preservedStates.pop()).setSelectorOnClickListener(null);
        }
    }

    public void setVisible(boolean visible, boolean animated) {
        if (!visible || getVisibility() != 0) {
            if ((visible || getVisibility() == 0) && this._inTransition.block(1)) {
                cancelAnimations();
                setVisibility(visible ? 0 : 4);
                requestLayout();
                if (this._visibilityListener != null) {
                    this._visibilityListener.onVisibilityChanged(visible);
                }
            }
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (this._itemSelectorPanel != null) {
            ToolButton contextButton = this._itemSelectorPanel.getContextButton();
            if (contextButton != null) {
                contextButton.setEnabled(enabled);
            }
            Iterator i$ = this._itemSelectorPanel._itemViews.iterator();
            while (i$.hasNext()) {
                ((View) i$.next()).setEnabled(enabled);
            }
        }
    }

    private void cancelAnimations() {
        if (!this._inTransition.block(1)) {
            if (this._inAnimation != null) {
                this._inAnimation.cancel();
            }
            if (this._outAnimation != null) {
                this._outAnimation.cancel();
            }
            invalidate();
        }
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (getHeight() != getResources().getDimensionPixelSize(R.dimen.tb_subpanel_shadow_height)) {
            this._panelContainer.setBackgroundDrawable(createPanelBackgroundDrawable(this._panelContainer.getMeasuredWidth(), this._panelContainer.getMeasuredHeight()));
        }
    }

    private BitmapDrawable createPanelBackgroundDrawable(int width, int height) {
        if (width <= 0 || height <= 0) {
            return null;
        }
        Resources resources = getResources();
        int shadowHeight = resources.getDimensionPixelSize(R.dimen.tb_subpanel_shadow_height);
        Bitmap backgroundBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(backgroundBitmap);
        Drawable shadow = resources.getDrawable(R.drawable.schlagschatten);
        shadow.setBounds(new Rect(0, 0, width, shadowHeight));
        shadow.draw(canvas);
        Paint paint = new Paint();
        paint.setColor(resources.getColor(R.color.tb_subpanel_background));
        canvas.drawRect(new Rect(0, shadowHeight, width, height), paint);
        paint.setColor(-1773121456);
        paint.setStrokeWidth(1.0f);
        canvas.drawLine(0.0f, (float) shadowHeight, (float) width, (float) shadowHeight, paint);
        paint.setColor(-16777216);
        canvas.drawLine(0.0f, (float) (shadowHeight - 1), (float) width, (float) (shadowHeight - 1), paint);
        return new BitmapDrawable(resources, backgroundBitmap);
    }
}

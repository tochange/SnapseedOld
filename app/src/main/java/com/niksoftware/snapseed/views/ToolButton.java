package com.niksoftware.snapseed.views;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.text.TextUtils.TruncateAt;
import android.util.AttributeSet;
import android.util.StateSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.niksoftware.snapseed.R;

import java.security.InvalidParameterException;

public class ToolButton extends RelativeLayout {
    protected static int DISABLED_STATE_DRAWABLE_ALPHA = 160;
    protected ImageView imageView;
    protected int maxWidth;
    protected int titleSpacing;
    protected TextView titleView;

    public ToolButton(Context context) {
        super(context);
        init(context);
        setTitleVisible(false);
    }

    public ToolButton(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init(context);
        int styleId = attributeSet.getStyleAttribute();
        if (styleId != 0) {
            setStyle(styleId);
        }
        setBackgroundDrawable(context.getResources().getDrawable(R.drawable.tb_button_background));
        TypedArray customAttributes = context.obtainStyledAttributes(attributeSet, R.styleable.ToolbarButton);
        boolean titleVisible = true;
        int count = customAttributes != null ? customAttributes.getIndexCount() : 0;
        if (count > 0) {
            StateListDrawable states = new StateListDrawable();
            boolean hasDisabledState = false;
            Drawable normalDrawable = null;
            for (int i = 0; i < count; i++) {
                int attribute = customAttributes.getIndex(i);
                switch (attribute) {
                    case 0:
                        setText(customAttributes.getString(attribute));
                        break;
                    case 1:
                        titleVisible = customAttributes.getBoolean(attribute, true);
                        break;
                    case 2:
                        normalDrawable = customAttributes.getDrawable(attribute);
                        break;
                    case 3:
                        states.addState(new int[]{16842913}, customAttributes.getDrawable(attribute));
                        break;
                    case 4:
                        hasDisabledState = true;
                        states.addState(new int[]{-16842910}, customAttributes.getDrawable(attribute));
                        break;
                    default:
                        throw new InvalidParameterException("Unsupported custom attribute found!");
                }
            }
            if (normalDrawable != null) {
                if (!hasDisabledState) {
                    states.addState(new int[]{-16842910}, genDisabledStateDrawable(normalDrawable));
                }
                states.addState(StateSet.WILD_CARD, normalDrawable);
            }
            this.imageView.setImageDrawable(states);
        }
        setTitleVisible(titleVisible);
    }

    protected void init(Context context) {
        Resources resources = getResources();
        this.imageView = new ImageView(context);
        this.imageView.setId(16908294);
        this.imageView.setBackgroundColor(resources.getColor(17170445));
        this.titleView = new TextView(context);
        this.imageView.setId(16908308);
        this.imageView.setDuplicateParentStateEnabled(true);
        this.titleView.setDuplicateParentStateEnabled(true);
        addView(this.imageView);
        addView(this.titleView);
        this.titleSpacing = resources.getDimensionPixelSize(R.dimen.tb_button_title_spacing);
        setClickable(true);
    }

    public void setText(String title) {
        TextView textView = this.titleView;
        if (title == null) {
            title = "";
        }
        textView.setText(title);
    }

    public void setText(int resId) {
        this.titleView.setText(resId);
    }

    public CharSequence getTitle() {
        return this.titleView.getText();
    }

    public void setTitleVisible(boolean visible) {
        this.titleView.setVisibility(visible ? 0 : 8);
        LayoutParams layoutParams1;
        if (visible) {
            layoutParams1 = new LayoutParams(-2, -2);
            layoutParams1.addRule(10, -1);
            layoutParams1.addRule(14, -1);
            this.imageView.setLayoutParams(layoutParams1);
            LayoutParams layoutParams2 = new LayoutParams(-2, -2);
            layoutParams2.addRule(3, this.imageView.getId());
            layoutParams2.addRule(14, this.imageView.getId());
            layoutParams2.topMargin = this.titleSpacing;
            this.titleView.setLayoutParams(layoutParams2);
        } else {
            layoutParams1 = new LayoutParams(-2, -2);
            layoutParams1.addRule(13, -1);
            this.imageView.setLayoutParams(layoutParams1);
        }
        requestLayout();
    }

    public boolean isTitleVisible() {
        return this.titleView.getVisibility() == 0;
    }

    public void setTitleSpacing(int titleSpacing) {
        this.titleSpacing = titleSpacing;
        if (isTitleVisible()) {
            setTitleVisible(true);
        }
    }

    public void setStateImages(int normalId, int selectedId, int disabledId) {
        if (this.imageView != null && normalId != 0) {
            Resources resources = getResources();
            StateListDrawable states = new StateListDrawable();
            if (selectedId != 0) {
                states.addState(new int[]{16842913}, resources.getDrawable(selectedId));
            }
            Drawable normalDrawable = resources.getDrawable(normalId);
            states.addState(new int[]{-16842910}, disabledId == 0 ? genDisabledStateDrawable(normalDrawable) : resources.getDrawable(disabledId));
            states.addState(StateSet.WILD_CARD, normalDrawable);
            this.imageView.setImageDrawable(states);
        }
    }

    public void setStateImages(Bitmap normalBitmap, Bitmap selectedBitmap, Bitmap disabledBitmap) {
        if (this.imageView != null && normalBitmap != null) {
            Resources resources = getResources();
            StateListDrawable states = new StateListDrawable();
            if (selectedBitmap != null) {
                states.addState(new int[]{16842913}, new BitmapDrawable(resources, selectedBitmap));
            }
            Drawable normalDrawable = new BitmapDrawable(resources, normalBitmap);
            states.addState(new int[]{-16842910}, disabledBitmap == null ? genDisabledStateDrawable(normalDrawable) : new BitmapDrawable(resources, disabledBitmap));
            states.addState(StateSet.WILD_CARD, normalDrawable);
            this.imageView.setImageDrawable(states);
        }
    }

    public void setStateImagesTintColor(int normalId, int selectedTintColor, int disabledTintColor) {
        if (this.imageView != null && normalId != 0) {
            setStateImagesTintColor(BitmapFactory.decodeResource(getResources(), normalId), selectedTintColor, disabledTintColor);
        }
    }

    public void setStateImagesTintColor(Bitmap normalBitmap, int selectedTintColor, int disabledTintColor) {
        if (this.imageView != null && normalBitmap != null) {
            Resources resources = getResources();
            StateListDrawable states = new StateListDrawable();
            Drawable normalDrawable = new BitmapDrawable(resources, normalBitmap);
            if (selectedTintColor != 0) {
                Drawable pressedDrawable = genTintedDrawable(normalDrawable, selectedTintColor);
                states.addState(new int[]{16842913}, pressedDrawable);
                states.addState(new int[]{16842919}, pressedDrawable);
            }
            states.addState(new int[]{-16842910}, disabledTintColor == 0 ? normalDrawable : genTintedDrawable(normalDrawable, disabledTintColor));
            states.addState(StateSet.WILD_CARD, normalDrawable);
            this.imageView.setImageDrawable(states);
        }
    }

    public void setStyle(int styleId) {
        this.titleView.setTextAppearance(getContext(), styleId);
        this.titleView.setShadowLayer(1.0f, 0.0f, 1.0f, -436207617);
    }

    public void setStyleNoShadow(int styleId) {
        this.titleView.setTextAppearance(getContext(), styleId);
        this.titleView.setShadowLayer(0.0f, 0.0f, 0.0f, 0);
    }

    public void setMaxTextWidth(int maxWidth, TruncateAt truncateAt) {
        if (this.titleView != null) {
            this.maxWidth = maxWidth;
            if (this.maxWidth > 0) {
                this.titleView.setSingleLine(true);
                this.titleView.setMaxWidth(maxWidth);
                this.titleView.setEllipsize(truncateAt);
                return;
            }
            this.titleView.setSingleLine(false);
            this.titleView.setEllipsize(null);
        }
    }

    protected BitmapDrawable genDisabledStateDrawable(Drawable normalStateDrawable) {
        int width = normalStateDrawable.getIntrinsicWidth();
        int height = normalStateDrawable.getIntrinsicHeight();
        Bitmap disabledBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(disabledBitmap);
        normalStateDrawable.setBounds(0, 0, width, height);
        normalStateDrawable.setAlpha(DISABLED_STATE_DRAWABLE_ALPHA);
        normalStateDrawable.draw(canvas);
        return new BitmapDrawable(getResources(), disabledBitmap);
    }

    protected BitmapDrawable genTintedDrawable(Drawable normalDrawable, int tintColor) {
        int width = normalDrawable.getIntrinsicWidth();
        int height = normalDrawable.getIntrinsicHeight();
        Bitmap disabledBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(disabledBitmap);
        normalDrawable.setBounds(0, 0, width, height);
        normalDrawable.setColorFilter(new LightingColorFilter(tintColor, 1));
        normalDrawable.draw(canvas);
        normalDrawable.setColorFilter(null);
        return new BitmapDrawable(getResources(), disabledBitmap);
    }
}

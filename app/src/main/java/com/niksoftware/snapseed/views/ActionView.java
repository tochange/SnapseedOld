package com.niksoftware.snapseed.views;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.SystemClock;
import android.view.View;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;

public class ActionView extends View {
    private static final int INVALIDATE_DELAY = 50;
    private static final int MESSAGE_FADE_DELAY = 500;
    private ObjectAnimator _alphaDown;
    private ObjectAnimator _alphaUp;
    private int _backgroundBoundsUpdateThreshold;
    private float _backgroundCornerRadius;
    private Paint _backgroundFillPaint;
    private Paint _backgroundStrokePaint;
    private int _backgroundStrokeWidth;
    private Runnable _delayedHiding = new Runnable() {
        public void run() {
            ActionView.this.hide(true);
        }
    };
    private final Runnable _delayedUpdate = new Runnable() {
        public void run() {
            ActionView.this.invalidateDelayed();
        }
    };
    private boolean _hiddenExceptForUndo;
    private boolean _isVisible = false;
    private RectF _lastBackgroundRect = new RectF();
    private long _lastUpdate = 0;
    private String _text = "";
    private Rect _textBounds = new Rect();
    private int _textPadding;
    private Paint _textPaint;
    private int _textTop = -1;

    public ActionView(Context context) {
        super(context);
        Resources resources = getResources();
        this._textPadding = resources.getDimensionPixelSize(R.dimen.wa_action_view_text_padding);
        this._backgroundBoundsUpdateThreshold = resources.getDimensionPixelSize(R.dimen.wa_action_view_bg_update_threshold);
        this._backgroundCornerRadius = ((float) resources.getDimensionPixelSize(R.dimen.tmp_active_param_item_height)) / 2.0f;
        this._backgroundStrokeWidth = resources.getDimensionPixelSize(R.dimen.wa_action_view_stroke_width);
        float shadowSize = resources.getDimension(R.dimen.parameter_title_shadow_size);
        this._textPaint = new Paint();
        this._textPaint.setColor(-1073741825);
        this._textPaint.setTextAlign(Align.CENTER);
        this._textPaint.setAntiAlias(true);
        this._textPaint.setSubpixelText(true);
        this._textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        this._textPaint.setTextSize((float) ParameterViewHelper.TEXT_SIZE());
        this._textPaint.setShadowLayer(shadowSize, 0.0f, -shadowSize, -1090519040);
        this._backgroundFillPaint = new Paint();
        this._backgroundFillPaint.setAntiAlias(true);
        this._backgroundFillPaint.setColor(-1641272276);
        this._backgroundFillPaint.setStyle(Style.FILL);
        this._backgroundStrokePaint = new Paint();
        this._backgroundStrokePaint.setAntiAlias(true);
        this._backgroundStrokePaint.setColor(1895825407);
        this._backgroundStrokePaint.setStyle(Style.STROKE);
        this._backgroundStrokePaint.setStrokeWidth((float) this._backgroundStrokeWidth);
        NotificationCenter.getInstance().addListener(new NotificationCenterListener() {
            public void performAction(Object arg) {
                if (arg != null && !ActionView.this._hiddenExceptForUndo) {
                    ActionView.this.setMessage(MainActivity.getFilterParameter().getParameterDescription(ActionView.this.getContext(), ((Integer) arg).intValue()));
                }
            }
        }, ListenerType.DidChangeFilterParameterValue);
        this._alphaDown = ObjectAnimator.ofFloat(this, "alpha", new float[]{1.0f, 0.0f});
        this._alphaDown.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                ActionView.this.hide(false);
            }

            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }
        });
        this._alphaUp = ObjectAnimator.ofFloat(this, "alpha", new float[]{0.0f, 1.0f});
        this._alphaUp.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                ActionView.this._isVisible = true;
                ActionView.this.setVisibility(0);
            }

            public void onAnimationRepeat(Animator animation) {
            }

            public void onAnimationEnd(Animator animation) {
                if (!ActionView.this._isVisible) {
                    ActionView.this.setVisibility(8);
                }
            }

            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }
        });
    }

    public void setMessage(int messageResId) {
        setMessage(getResources().getString(messageResId));
    }

    public void setMessage(String message) {
        if (message != null && message.length() > 0) {
            this._text = message.toUpperCase();
            show();
        }
    }

    public static String formatParamValueMessage(String paramName, int paramValue) {
        return String.format("%s %+d", new Object[]{paramName, Integer.valueOf(paramValue)});
    }

    private void invalidateDelayed() {
        long now = SystemClock.elapsedRealtime();
        if (now - this._lastUpdate < 50) {
            postDelayed(this._delayedUpdate, 50);
            return;
        }
        removeCallbacks(this._delayedUpdate);
        this._lastUpdate = now;
        invalidate();
    }

    private void show() {
        if (this._isVisible) {
            invalidateDelayed();
        } else {
            removeCallbacks(this._delayedHiding);
            this._alphaUp.start();
        }
        hideDelayed(MESSAGE_FADE_DELAY);
    }

    public void hide(boolean animated) {
        if (!this._isVisible) {
            return;
        }
        if (animated) {
            this._alphaDown.start();
            return;
        }
        this._isVisible = false;
        setVisibility(8);
    }

    private void hideDelayed(int delay) {
        removeCallbacks(this._delayedHiding);
        postDelayed(this._delayedHiding, (long) delay);
    }

    public void setHiddenExceptForUndo(boolean hiddenExceptForUndo) {
        this._hiddenExceptForUndo = hiddenExceptForUndo;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getResources().getDimensionPixelSize(R.dimen.wa_max_action_view_width), getResources().getDimensionPixelSize(R.dimen.tmp_active_param_item_height));
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Rect textBounds = new Rect();
        this._textPaint.getTextBounds("0", 0, 1, textBounds);
        this._textTop = ((bottom - top) + textBounds.height()) / 2;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!this._text.isEmpty()) {
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            this._textPaint.getTextBounds(this._text, 0, this._text.length(), this._textBounds);
            float strokeInset = (float) this._backgroundStrokeWidth;
            RectF backgroundRect = new RectF(strokeInset, strokeInset, (float) (this._textBounds.width() + (this._textPadding * 2)), ((float) height) - strokeInset);
            float backgroundWidth = backgroundRect.width();
            backgroundRect.offset((((float) width) - backgroundWidth) / 2.0f, 0.0f);
            if (this._lastBackgroundRect.width() > 0.0f && Math.abs(this._lastBackgroundRect.width() - backgroundWidth) < ((float) this._backgroundBoundsUpdateThreshold)) {
                backgroundRect = this._lastBackgroundRect;
            }
            canvas.drawRoundRect(backgroundRect, this._backgroundCornerRadius, this._backgroundCornerRadius, this._backgroundFillPaint);
            canvas.drawRoundRect(backgroundRect, this._backgroundCornerRadius, this._backgroundCornerRadius, this._backgroundStrokePaint);
            this._lastBackgroundRect = backgroundRect;
            canvas.drawText(this._text, (float) (width / 2), (float) this._textTop, this._textPaint);
        }
    }
}

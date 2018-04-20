package com.niksoftware.snapseed.views;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

public class PopoverWindowView {
    protected Drawable _background = null;
    protected Context _context;
    protected View _rootView;
    protected WindowManager _windowManager;
    protected PopupWindow _wnd;

    public PopoverWindowView(Context context) {
        this._context = context;
        this._wnd = new PopupWindow(context);
        this._wnd.setTouchInterceptor(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() != 4) {
                    return false;
                }
                PopoverWindowView.this._wnd.dismiss();
                return true;
            }
        });
        this._windowManager = (WindowManager) context.getSystemService("window");
    }

    protected void onDismiss() {
    }

    protected void onShow() {
    }

    protected void preShow() {
        if (this._rootView == null) {
            throw new IllegalStateException("setContentView was not called with a view to display.");
        }
        onShow();
        if (this._background == null) {
            this._wnd.setBackgroundDrawable(new BitmapDrawable(this._rootView.getResources()));
        } else {
            this._wnd.setBackgroundDrawable(this._background);
        }
        this._wnd.setWidth(-2);
        this._wnd.setHeight(-2);
        this._wnd.setTouchable(true);
        this._wnd.setFocusable(true);
        this._wnd.setOutsideTouchable(true);
        this._wnd.setContentView(this._rootView);
    }

    public void setBackgroundDrawable(Drawable background) {
        this._background = background;
    }

    public void setContentView(View root) {
        this._rootView = root;
        this._wnd.setContentView(root);
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this._wnd.setOnDismissListener(listener);
    }

    public void dismiss() {
        this._wnd.dismiss();
    }
}

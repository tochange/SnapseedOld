package com.niksoftware.snapseed.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

public class ImageViewSW extends View {
    private boolean _fillBackground = true;

    public ImageViewSW(Context context) {
        super(context);
    }

    public void setImageBitmap(Bitmap image) {
        if (image == null || image.isRecycled()) {
            setBackgroundDrawable(null);
        } else {
            BitmapDrawable draw = new BitmapDrawable(getResources(), image);
            draw.setAlpha(255);
            draw.setAntiAlias(false);
            draw.setDither(false);
            setBackgroundDrawable(draw);
        }
        invalidate();
    }

    public void setFillBackground(boolean fillBackground) {
        this._fillBackground = fillBackground;
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        Drawable background = getBackground();
        if (background != null) {
            if (this._fillBackground) {
                canvas.drawColor(-16777216);
            }
            background.setBounds(0, 0, getWidth(), getHeight());
            background.draw(canvas);
        }
    }
}

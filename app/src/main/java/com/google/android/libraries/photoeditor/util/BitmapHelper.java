package com.google.android.libraries.photoeditor.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

public final class BitmapHelper {
    private BitmapHelper() {
    }

    public static Bitmap cropAndBorderBitmap(Bitmap bitmap, int width, int height, int edgeSpace, int borderWidth, int borderColor) {
        int bitmapWidth = bitmap.getWidth();
        int bitmapHeight = bitmap.getHeight();
        float ratio = Math.max(((float) width) / ((float) bitmapWidth), ((float) height) / ((float) bitmapHeight));
        if (ratio < 1.0f) {
            ratio = 1.0f;
        }
        Rect destRect = new Rect(0, 0, width - (edgeSpace * 2), height - (edgeSpace * 2));
        Rect sourceRect = new Rect(0, 0, (int) Math.floor((double) (((float) destRect.width()) / ratio)), (int) Math.floor((double) (((float) destRect.height()) / ratio)));
        destRect.offset(edgeSpace, edgeSpace);
        sourceRect.offset((bitmapWidth - sourceRect.width()) / 2, (bitmapHeight - sourceRect.height()) / 2);
        Bitmap result = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        canvas.drawBitmap(bitmap, sourceRect, destRect, paint);
        if (borderWidth > 0) {
            paint.setStrokeWidth((float) borderWidth);
            paint.setStyle(Style.STROKE);
            paint.setColor(borderColor);
            canvas.drawRect(((float) edgeSpace) + (((float) borderWidth) / 2.0f), ((float) edgeSpace) + (((float) borderWidth) / 2.0f), (((float) width) - (((float) borderWidth) / 2.0f)) - ((float) edgeSpace), (((float) height) - (((float) borderWidth) / 2.0f)) - ((float) edgeSpace), paint);
        }
        return result;
    }

    public static Bitmap composeBitmaps(Bitmap rootBitmap, Bitmap overlayBitmap) {
        if (rootBitmap == null || overlayBitmap == null) {
            return null;
        }
        int width = Math.max(rootBitmap.getWidth(), overlayBitmap.getWidth());
        int height = Math.max(rootBitmap.getHeight(), overlayBitmap.getHeight());
        Bitmap composedBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(composedBitmap);
        Paint paint = new Paint();
        canvas.drawBitmap(rootBitmap, ((float) (width - rootBitmap.getWidth())) / 2.0f, ((float) (height - rootBitmap.getHeight())) / 2.0f, paint);
        canvas.drawBitmap(overlayBitmap, ((float) (width - overlayBitmap.getWidth())) / 2.0f, ((float) (height - overlayBitmap.getHeight())) / 2.0f, paint);
        return composedBitmap;
    }

    public static Bitmap composeBitmaps(Bitmap rootBitmap, Drawable overlayDrawable) {
        if (rootBitmap == null || overlayDrawable == null) {
            return null;
        }
        int width = rootBitmap.getWidth();
        int height = rootBitmap.getHeight();
        overlayDrawable.setBounds(0, 0, width, height);
        Bitmap composedBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(composedBitmap);
        canvas.drawBitmap(rootBitmap, 0.0f, 0.0f, new Paint());
        overlayDrawable.draw(canvas);
        return composedBitmap;
    }

    public static Bitmap convertToRGBA8(Bitmap source) {
        if (source.getConfig() == Config.ARGB_8888) {
            return source;
        }
        Bitmap destination = source.copy(Config.ARGB_8888, false);
        source.recycle();
        return destination;
    }
}

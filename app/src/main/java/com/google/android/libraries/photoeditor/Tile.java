package com.google.android.libraries.photoeditor;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;

public class Tile {
    public final Rect scaledPosition;
    public final Rect sourcePosition;
    public int srcTexture = -1;
    public final int x;
    public final int y;

    Tile(Rect sourcePosition, Rect scaledPosition, int xIndex, int yIndex) {
        this.sourcePosition = sourcePosition;
        this.scaledPosition = scaledPosition;
        this.x = xIndex;
        this.y = yIndex;
    }

    public void deleteTexture() {
        if (this.srcTexture >= 0) {
            TilesProvider.addTextureToClean(this.srcTexture);
            this.srcTexture = -1;
        }
    }

    public void createTexture(Bitmap scaledBitmap) {
        if (this.srcTexture == -1) {
            if (scaledBitmap.getConfig() != Config.ARGB_8888) {
                throw new IllegalArgumentException("Invalid pixel format");
            }
            this.srcTexture = NativeCore.createRGBX8TextureFromBitmap(9728, 6408, 33071, scaledBitmap, this.scaledPosition.left, this.scaledPosition.top, this.scaledPosition.width(), this.scaledPosition.height());
        }
    }

    public int getScaledX() {
        return this.scaledPosition.left;
    }

    public int getScaledY() {
        return this.scaledPosition.top;
    }

    public int getScaledWidth() {
        return this.scaledPosition.width();
    }

    public int getScaledHeight() {
        return this.scaledPosition.height();
    }
}

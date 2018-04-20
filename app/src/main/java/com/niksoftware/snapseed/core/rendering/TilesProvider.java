package com.niksoftware.snapseed.core.rendering;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;
import android.util.FloatMath;
import android.util.Log;
import com.niksoftware.snapseed.core.NativeCore;
import com.niksoftware.snapseed.core.RenderFilterInterface.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class TilesProvider implements DataSource {
    private static final String LOG_TAG = "TilesProvider";
    private static final int PREVIEW_TEXTURE_MAX_SIZE = 512;
    public static final int PREVIEW_TILE_SIZE = 1024;
    private static final float STYLE_IMAGE_SIZE = 128.0f;
    private static final List<Integer> texturesToClean = new ArrayList();
    private boolean isExportTilesProvider;
    private final ReentrantLock lock = new ReentrantLock();
    private int previewHeight;
    private Bitmap previewSourceImage;
    private int previewTexture = -1;
    private int previewWidth;
    private int scaledHeight = 0;
    private int scaledWidth = 0;
    private Bitmap screenSourceImage;
    private Bitmap sourceImage;
    private Bitmap styleSourceImage;
    private int tileSize;
    private final List<Tile> tiles = new ArrayList();
    private float zoom = -1.0f;

    public TilesProvider(Bitmap sourceImage, Bitmap screenImage, int tileSize, boolean isExportTilesProvider) {
        if (sourceImage.getConfig() != Config.ARGB_8888) {
            throw new IllegalArgumentException("Invalid source pixel format");
        }
        this.sourceImage = sourceImage;
        this.screenSourceImage = screenImage;
        this.isExportTilesProvider = isExportTilesProvider;
        this.tileSize = tileSize;
        int sourceWidth = sourceImage.getWidth();
        int sourceHeight = sourceImage.getHeight();
        if (!this.isExportTilesProvider) {
            float scale = Math.max(((float) sourceWidth) / 512.0f, ((float) sourceHeight) / 512.0f);
            this.previewWidth = Math.min(Math.round(((float) sourceWidth) / scale), PREVIEW_TEXTURE_MAX_SIZE);
            this.previewHeight = Math.min(Math.round(((float) sourceHeight) / scale), PREVIEW_TEXTURE_MAX_SIZE);
            if (this.previewWidth > sourceWidth) {
                this.previewWidth = sourceWidth;
            }
            if (this.previewHeight > sourceHeight) {
                this.previewHeight = sourceHeight;
            }
            this.previewSourceImage = Bitmap.createBitmap(this.previewWidth, this.previewHeight, Config.ARGB_8888);
            NativeCore.scaleImage(this.sourceImage, this.previewSourceImage);
            if (this.previewSourceImage == this.sourceImage) {
                this.previewSourceImage = this.sourceImage.copy(Config.ARGB_8888, false);
            }
        }
    }

    public void cleanup() {
        if (!this.isExportTilesProvider) {
            if (!(this.previewSourceImage == null || this.previewSourceImage == this.sourceImage || this.previewSourceImage.isRecycled())) {
                this.previewSourceImage.recycle();
            }
            if (!(this.sourceImage == null || this.sourceImage.isRecycled())) {
                this.sourceImage.recycle();
            }
        }
        cleanupTiles(false);
        cleanupPreviewTexture();
        this.previewSourceImage = null;
        this.sourceImage = null;
        this.styleSourceImage = null;
        this.screenSourceImage = null;
    }

    public void cleanupGL() {
        cleanupTiles(true);
        cleanupPreviewTexture();
    }

    private void cleanupPreviewTexture() {
        if (this.previewTexture >= 0) {
            addTextureToClean(this.previewTexture);
            this.previewTexture = -1;
        }
    }

    private void cleanupTiles(boolean cleanTexturesOnly) {
        lock();
        for (Tile tile : this.tiles) {
            tile.deleteTexture();
        }
        if (!cleanTexturesOnly) {
            this.tiles.clear();
            this.scaledWidth = 0;
            this.scaledHeight = 0;
        }
        unlock();
    }

    private Bitmap createStyleImage() {
        float scale = Math.max(((float) getPreviewWidth()) / STYLE_IMAGE_SIZE, ((float) getPreviewHeight()) / STYLE_IMAGE_SIZE);
        return Bitmap.createScaledBitmap(this.previewSourceImage, Math.round(((float) getPreviewWidth()) / scale), Math.round(((float) getPreviewHeight()) / scale), true);
    }

    public void lock() {
        this.lock.lock();
    }

    public void unlock() {
        this.lock.unlock();
    }

    public boolean setZoomScale(float zoomScale) {
        if (zoomScale > 1.0f) {
            zoomScale = 1.0f;
        }
        return Math.abs(zoomScale - this.zoom) >= 0.001f && setZoomSize(Math.round(((float) this.sourceImage.getWidth()) * zoomScale), Math.round(((float) this.sourceImage.getHeight()) * zoomScale));
    }

    public boolean setZoomSize(int newScaledWidth, int newScaledHeight) {
        if (newScaledWidth == this.scaledWidth && newScaledHeight == this.scaledHeight) {
            return false;
        }
        lock();
        cleanupTiles(false);
        int imageWidth = this.sourceImage.getWidth();
        int imageHeight = this.sourceImage.getHeight();
        this.zoom = Math.min(((float) newScaledWidth) / ((float) imageWidth), ((float) newScaledHeight) / ((float) imageHeight));
        int xCount = Math.max((int) FloatMath.ceil(((float) newScaledWidth) / ((float) this.tileSize)), 1);
        int yCount = Math.max((int) FloatMath.ceil(((float) newScaledHeight) / ((float) this.tileSize)), 1);
        int yScaledPos = 0;
        int y100Pos = 0;
        int sourceTileSize = Math.round(((float) this.tileSize) / this.zoom);
        for (int yIndex = 0; yIndex < yCount; yIndex++) {
            int xScaledPos = 0;
            int x100Pos = 0;
            Rect scaledRect = null;
            Rect hundredRect = null;
            for (int xIndex = 0; xIndex < xCount; xIndex++) {
                scaledRect = new Rect(xScaledPos, yScaledPos, this.tileSize + xScaledPos, this.tileSize + yScaledPos);
                if (scaledRect.right > newScaledWidth) {
                    scaledRect.right = scaledRect.left + (newScaledWidth - xScaledPos);
                }
                if (scaledRect.bottom > newScaledHeight) {
                    scaledRect.bottom = scaledRect.top + (newScaledHeight - yScaledPos);
                }
                hundredRect = new Rect(x100Pos, y100Pos, x100Pos + sourceTileSize, y100Pos + sourceTileSize);
                if (hundredRect.right > imageWidth) {
                    hundredRect.right = hundredRect.left + (imageWidth - x100Pos);
                }
                if (hundredRect.bottom > imageHeight) {
                    hundredRect.bottom = hundredRect.top + (imageHeight - y100Pos);
                }
                if (scaledRect.width() <= 0 || scaledRect.height() <= 0) {
                    throw new IllegalStateException("Invalid scaled tile rectangle");
                }
                Tile tile = new Tile(hundredRect, scaledRect, xIndex, yIndex);
                this.tiles.add(tile);
                this.scaledWidth = Math.max(tile.scaledPosition.right, this.scaledWidth);
                this.scaledHeight = Math.max(tile.scaledPosition.bottom, this.scaledHeight);
                xScaledPos = scaledRect.right;
                x100Pos = hundredRect.right;
            }
            if (scaledRect != null) {
                yScaledPos = scaledRect.bottom;
            }
            if (hundredRect != null) {
                y100Pos = hundredRect.bottom;
            }
        }
        unlock();
        return true;
    }

    public float getZoom() {
        return this.zoom;
    }

    public Bitmap getScreenSourceImage() {
        return this.screenSourceImage;
    }

    public Bitmap getSourceImage() {
        return this.sourceImage;
    }

    public void setSourceImage(Bitmap bitmap) {
        this.sourceImage = bitmap;
    }

    public Bitmap getPreviewSourceImage() {
        return this.previewSourceImage;
    }

    public Bitmap getStyleSourceImage() {
        if (this.styleSourceImage == null) {
            this.styleSourceImage = createStyleImage();
        }
        return this.styleSourceImage;
    }

    public int getHundredPercentWidth() {
        return this.sourceImage.getWidth();
    }

    public int getHundredPercentHeight() {
        return this.sourceImage.getHeight();
    }

    public int getScaledWidth() {
        return this.scaledWidth;
    }

    public int getScaledHeight() {
        return this.scaledHeight;
    }

    public int getTileSize() {
        return this.tileSize;
    }

    public int getPreviewWidth() {
        return this.previewWidth;
    }

    public int getPreviewHeight() {
        return this.previewHeight;
    }

    public int getPreviewSrcTexture() {
        return this.previewTexture;
    }

    public void createPreviewTexture() {
        if (this.previewTexture < 0) {
            this.previewTexture = NativeCore.createRGBX8TextureFromBitmap(9729, 6408, 33071, this.previewSourceImage);
        }
    }

    public boolean needsCreateSourceTextures() {
        lock();
        boolean texturesNeeded = false;
        for (Tile tile : this.tiles) {
            if (tile.srcTexture == -1) {
                texturesNeeded = true;
                break;
            }
        }
        unlock();
        return texturesNeeded;
    }

    public void createSourceTileTextures() {
        if (needsCreateSourceTextures()) {
            lock();
            Bitmap scaledBitmap = Bitmap.createScaledBitmap(this.sourceImage, this.scaledWidth, this.scaledHeight, true);
            if (scaledBitmap.getConfig() != Config.ARGB_8888) {
                Bitmap newBitmap = scaledBitmap.copy(Config.ARGB_8888, false);
                scaledBitmap.recycle();
                scaledBitmap = newBitmap;
            }
            if (this.tiles.size() == 1 && ((Tile) this.tiles.get(0)).getScaledWidth() == this.scaledWidth && ((Tile) this.tiles.get(0)).getScaledHeight() == this.scaledHeight) {
                ((Tile) this.tiles.get(0)).srcTexture = NativeCore.createRGBX8TextureFromBitmap(9728, 6408, 33071, scaledBitmap);
            } else {
                for (Tile tile : this.tiles) {
                    tile.createTexture(scaledBitmap);
                }
            }
            if (scaledBitmap != this.sourceImage) {
                scaledBitmap.recycle();
            }
            unlock();
        }
    }

    public List<Tile> getTiles() {
        return this.tiles;
    }

    public static void addTextureToClean(int texture) {
        texturesToClean.add(Integer.valueOf(texture));
    }

    public static void cleanTexturesToClean() {
        for (Integer texture : texturesToClean) {
            NativeCore.deleteTexture(texture.intValue());
        }
        texturesToClean.clear();
    }

    private void dump() {
        Log.v(LOG_TAG, String.format("xxx TilesProvider::dump - zoom:%f xxx", new Object[]{Float.valueOf(this.zoom)}));
        Log.v(LOG_TAG, String.format("xxx TilesProvider::dump hundredPercentSize - w:%d h:%d xxx", new Object[]{Integer.valueOf(this.sourceImage.getWidth()), Integer.valueOf(this.sourceImage.getHeight())}));
        Log.v(LOG_TAG, String.format("xxx TilesProvider::dump scaledSize - w:%d h:%d xxx", new Object[]{Integer.valueOf(this.scaledWidth), Integer.valueOf(this.scaledHeight)}));
        for (Tile tile : this.tiles) {
            Log.v(LOG_TAG, String.format("Tile: %d %d", new Object[]{Integer.valueOf(tile.x), Integer.valueOf(tile.y)}));
            Log.v(LOG_TAG, String.format("Tile sourcePosition: %d %d %d %d", new Object[]{Integer.valueOf(tile.sourcePosition.left), Integer.valueOf(tile.sourcePosition.top), Integer.valueOf(tile.sourcePosition.width()), Integer.valueOf(tile.sourcePosition.height())}));
            Log.v(LOG_TAG, String.format("Tile scaledPosition: %d %d %d %d", new Object[]{Integer.valueOf(tile.scaledPosition.left), Integer.valueOf(tile.scaledPosition.top), Integer.valueOf(tile.scaledPosition.width()), Integer.valueOf(tile.scaledPosition.height())}));
        }
    }

    public static int getExportTileSize(int filterType) {
        return filterType == 3 ? PREVIEW_TEXTURE_MAX_SIZE : 1024;
    }
}

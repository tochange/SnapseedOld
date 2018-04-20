package com.niksoftware.snapseed.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.SurfaceHolder;
import com.niksoftware.snapseed.core.NativeCore;
import com.niksoftware.snapseed.core.NativeCore.NativeCoreNotificationListener;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.RenderFilterInterface.DataSource;
import com.niksoftware.snapseed.core.RenderFilterInterface.ImageRenderer;
import com.niksoftware.snapseed.core.RenderFilterInterface.OnBatchRenderListener;
import com.niksoftware.snapseed.core.RenderFilterInterface.OnPreviewRenderListener;
import com.niksoftware.snapseed.core.RenderFilterInterface.OnRenderListener;
import com.niksoftware.snapseed.core.RenderFilterInterface.PreviewRenderer;
import com.niksoftware.snapseed.core.RenderFilterInterface.RendererLifecycleListener;
import com.niksoftware.snapseed.core.RenderFilterInterface.StyleRenderer;
import com.niksoftware.snapseed.core.filterparameters.EmptyFilterParameter;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.core.rendering.GeometryObject;
import com.niksoftware.snapseed.core.rendering.Tile;
import com.niksoftware.snapseed.core.rendering.TilesProvider;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL10;

public class ImageViewGL extends GLSurfaceView implements ImageRenderer, StyleRenderer, PreviewRenderer {
    private static final String LOG_TAG = "ImageViewGL";
    private boolean breakProcess;
    private boolean contextIsInitialized;
    final Runnable delayedUpdate = new Runnable() {
        public void run() {
            ImageViewGL.this.setShouldRenderHighResolution();
            ImageViewGL.this.requestRender();
        }
    };
    private boolean didApplyImage;
    private Hashtable<String, String> glProperties;
    private boolean highResolutionUpdate;
    private RendererLifecycleListener lifecycleListener;
    private final Set<OnPreviewRenderListener> permanentOnPreviewListeners = new HashSet();
    private FilterParameter previewFilter;
    private TilesProvider previewTilesProvider;
    private int renderImageQueueSize = 0;
    private Renderer renderer;
    private final Set<OnPreviewRenderListener> singleShotOnPreviewListeners = new HashSet();

    private class CleanupNativeCoreEvent implements Runnable {
        private CleanupNativeCoreEvent() {
        }

        public void run() {
            try {
                ImageViewGL.this.renderer.setFpsListener(null, 0);
                ImageViewGL.this.renderer.cleanupNativeCore(false);
            } catch (Exception exception) {
                ImageViewGL.this.renderer.handleException(exception);
            }
        }
    }

    private class ExportEvent implements Runnable {
        private final FilterParameter filterParameter;
        private final OnRenderListener listener;
        private final Rect sourceRect;
        private final TilesProvider tilesProvider;

        public ExportEvent(TilesProvider tilesProvider, Rect sourceRect, FilterParameter filterParameter, OnRenderListener listener) {
            synchronized (ImageViewGL.this) {
                ImageViewGL.access$404(ImageViewGL.this);
            }
            this.tilesProvider = tilesProvider;
            this.sourceRect = sourceRect;
            this.filterParameter = filterParameter;
            this.listener = listener;
        }

        public void run() {
            try {
                if (this.sourceRect == null) {
                    ImageViewGL.this.renderer.renderExportImage(this.tilesProvider, this.filterParameter, this.listener);
                } else {
                    ImageViewGL.this.renderer.renderImageArea(this.tilesProvider, this.sourceRect, this.filterParameter, this.listener);
                }
                synchronized (ImageViewGL.this) {
                    ImageViewGL.access$406(ImageViewGL.this);
                }
            } catch (Exception ex) {
                ImageViewGL.this.renderer.handleException(ex);
            }
        }
    }

    private class FpsCounter extends TimerTask {
        private int frameCount;
        private FpsListener listener;
        private long startTime;
        private Timer timer;

        public FpsCounter(FpsListener listener, int updateIntervalMsec) {
            if (listener == null) {
                throw new NullPointerException("Listener cannot be null");
            }
            this.timer = new Timer();
            this.timer.scheduleAtFixedRate(this, new Date(), (long) updateIntervalMsec);
            this.listener = listener;
        }

        public void onFrame() {
            this.frameCount++;
        }

        public void reset() {
            this.startTime = System.currentTimeMillis();
            this.frameCount = 0;
        }

        private void cleanup() {
            this.timer.cancel();
            this.listener = null;
        }

        public void run() {
            ImageViewGL.this.post(new Runnable() {
                public void run() {
                    long currentTime = System.currentTimeMillis();
                    FpsCounter.this.listener.onFpsUpdate(FpsCounter.this.frameCount > 0 ? ((float) (FpsCounter.this.frameCount * 1000)) / ((float) (currentTime - FpsCounter.this.startTime)) : 0.0f);
                    FpsCounter.this.startTime = currentTime;
                    FpsCounter.this.frameCount = 0;
                }
            });
        }
    }

    public interface FpsListener {
        void onFpsUpdate(float f);
    }

    private class Renderer implements android.opengl.GLSurfaceView.Renderer {
        private EGLContext currentContext;
        private EGLDisplay currentDisplay;
        private EGLSurface currentSurface;
        private FpsCounter fpsCounter;
        private ArrayList<GeometryObject> geometryObjects;
        private boolean needsEmptyFilterFrame;
        FilterParameter pendingExportFilter;
        OnRenderListener pendingExportListener;
        TilesProvider pendingExportTilesProvider;
        private int renderLoopCount;

        private Renderer() {
            this.needsEmptyFilterFrame = true;
            this.currentContext = EGL10.EGL_NO_CONTEXT;
            this.currentSurface = EGL10.EGL_NO_SURFACE;
            this.currentDisplay = EGL10.EGL_NO_DISPLAY;
            this.renderLoopCount = 2;
        }

        public void setFpsListener(FpsListener fpsListener, int updateIntervalMsec) {
            setFpsListener(fpsListener, updateIntervalMsec, false);
        }

        public void setFpsListener(FpsListener fpsListener, int updateIntervalMsec, boolean useContinuousRendering) {
            if (this.fpsCounter != null) {
                this.fpsCounter.cleanup();
                this.fpsCounter = null;
            }
            if (fpsListener != null && updateIntervalMsec > 0) {
                this.fpsCounter = new FpsCounter(fpsListener, updateIntervalMsec);
            }
            ImageViewGL imageViewGL = ImageViewGL.this;
            int i = (this.fpsCounter == null || !useContinuousRendering) ? 0 : 1;
            imageViewGL.setRenderMode(i);
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glPixelStorei(3333, 1);
            GLES20.glPixelStorei(3317, 1);
            GLES20.glDisable(2884);
            GLES20.glDisable(3042);
            GLES20.glDisable(3024);
            GLES20.glDisable(2960);
            GLES20.glDisable(2929);
            GLES20.glDisable(3089);
            GLES20.glDepthMask(false);
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            this.needsEmptyFilterFrame = true;
            this.renderLoopCount = 2;
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, ImageViewGL.this.getWidth(), ImageViewGL.this.getHeight());
            GLES20.glClear(16384);
            NativeCore.onSurfaceChanged();
            if (this.fpsCounter != null) {
                this.fpsCounter.reset();
            }
        }

        public void onDrawFrame(GL10 gl) {
            try {
                if (ImageViewGL.this.previewTilesProvider != null && ImageViewGL.this.previewFilter != null) {
                    TilesProvider.cleanTexturesToClean();
                    ImageViewGL.this.previewTilesProvider.lock();
                    if (ImageViewGL.this.previewTilesProvider.getSourceImage() == null) {
                        ImageViewGL.this.previewTilesProvider.unlock();
                        return;
                    }
                    FilterParameter filter;
                    ImageViewGL.this.previewTilesProvider.createPreviewTexture();
                    ImageViewGL.this.previewTilesProvider.setZoomSize(ImageViewGL.this.getWidth(), ImageViewGL.this.getHeight());
                    if (ImageViewGL.this.didApplyImage || this.needsEmptyFilterFrame) {
                        filter = EmptyFilterParameter.INSTANCE;
                    } else {
                        filter = ImageViewGL.this.previewFilter;
                    }
                    NativeCore.INSTANCE.activateOnScreenFilterChecked(filter);
                    if (this.renderLoopCount > 0) {
                        this.needsEmptyFilterFrame = false;
                        ImageViewGL.this.requestRender();
                        this.renderLoopCount--;
                    }
                    int filterType = filter.getFilterType();
                    if (filterType == 1) {
                        NativeCore.deactivateOffScreenFilter();
                    }
                    ImageViewGL.this.previewTilesProvider.createSourceTileTextures();
                    boolean renderHighResolution = ImageViewGL.this.shouldRenderHighResolution(filterType);
                    if (renderHighResolution) {
                        renderTiles(ImageViewGL.this.previewTilesProvider, ImageViewGL.this.previewFilter, true);
                    } else if (filterType == 7) {
                        renderTiles(ImageViewGL.this.previewTilesProvider, ImageViewGL.this.previewFilter, false);
                    } else {
                        NativeCore.onscreenFilterPreviewToScreen(ImageViewGL.this.previewFilter, ImageViewGL.this.previewTilesProvider, ImageViewGL.this.getWidth(), ImageViewGL.this.getHeight());
                    }
                    if (filterType != 1) {
                        renderOverlays();
                    }
                    ImageViewGL.this.previewTilesProvider.unlock();
                    ImageViewGL.this.onFrame(renderHighResolution, filterType);
                    if (this.fpsCounter != null) {
                        this.fpsCounter.onFrame();
                    }
                    if (this.pendingExportListener != null && !this.needsEmptyFilterFrame) {
                        ImageViewGL.this.requestRenderImage(this.pendingExportTilesProvider, null, this.pendingExportFilter, this.pendingExportListener, false);
                        this.pendingExportListener = null;
                        this.pendingExportTilesProvider = null;
                        this.pendingExportFilter = null;
                    }
                }
            } catch (Exception ex) {
                handleException(ex);
            }
        }

        private void renderTiles(TilesProvider tilesProvider, FilterParameter filter, boolean forceHighResolution) {
            boolean doPrefilter = true;
            int width = ImageViewGL.this.getWidth();
            int height = ImageViewGL.this.getHeight();
            for (Tile tile : tilesProvider.getTiles()) {
                NativeCore.onscreenFilterTileToScreen(filter, tilesProvider, tile, width, height, doPrefilter, forceHighResolution);
                doPrefilter = false;
            }
        }

        public synchronized void setGeometryObjects(ArrayList<GeometryObject> geometryObjects) {
            this.geometryObjects = geometryObjects;
        }

        private void renderOverlays() {
            if (!ImageViewGL.this.didApplyImage && this.geometryObjects != null) {
                synchronized (this) {
                    Iterator i$ = this.geometryObjects.iterator();
                    while (i$.hasNext()) {
                        ((GeometryObject) i$.next()).GLRender(ImageViewGL.this.getWidth(), ImageViewGL.this.getHeight());
                    }
                }
            }
        }

        private void renderExportImage(TilesProvider tilesProvider, FilterParameter filter, OnRenderListener listener) {
            if (!ImageViewGL.this.getPaused()) {
                if (this.needsEmptyFilterFrame) {
                    this.pendingExportListener = listener;
                    this.pendingExportTilesProvider = tilesProvider;
                    this.pendingExportFilter = filter;
                    ImageViewGL.this.requestRender();
                    return;
                }
                NativeCore.setRenderScaleMode(3);
                int filterType = filter.getFilterType();
                NativeCore.INSTANCE.setCompare(false);
                if (filterType != 3) {
                    storeEGL();
                    NativeCore.INSTANCE.activateOnScreenFilterChecked(filter);
                    NativeCore.onscreenPrefilter(filter, tilesProvider);
                    restoreEGL();
                    NativeCore.offscreenPrepareToApplyImage();
                }
                beginOffscreenRendering(tilesProvider, filterType);
                TilesProvider exportTilesProvider = new TilesProvider(tilesProvider.getSourceImage(), null, TilesProvider.getExportTileSize(filterType), true);
                exportTilesProvider.setZoomScale(1.0f);
                final int tileCount = exportTilesProvider.getTiles().size();
                final OnRenderListener onRenderListener = listener;
                ImageViewGL.this.post(new Runnable() {
                    public void run() {
                        onRenderListener.onRenderProgressUpdate(0, tileCount);
                    }
                });
                System.runFinalization();
                System.gc();
                int scaledWidth = exportTilesProvider.getScaledWidth();
                int scaledHeight = exportTilesProvider.getScaledHeight();
                NativeCore.allocateExportImage(scaledWidth, scaledHeight);
                boolean pause = false;
                NativeCore.offscreenPrefilter(filter, tilesProvider, exportTilesProvider);
                int currentTile = 0;
                for (Tile tile : exportTilesProvider.getTiles()) {
                    pause = ImageViewGL.this.getPaused();
                    if (pause) {
                        break;
                    }
                    tile.createTexture(exportTilesProvider.getSourceImage());
                    NativeCore.offscreenFilter(filter, tilesProvider, tile, exportTilesProvider);
                    tile.deleteTexture();
                    TilesProvider.cleanTexturesToClean();
                    currentTile++;
                    final int tileIndex = currentTile;
                    onRenderListener = listener;
                    ImageViewGL.this.post(new Runnable() {
                        public void run() {
                            onRenderListener.onRenderProgressUpdate(tileIndex, tileCount);
                        }
                    });
                }
                Bitmap exportedBitmap = null;
                if (!pause) {
                    tilesProvider.setSourceImage(null);
                    tilesProvider.cleanup();
                    exportedBitmap = exportTilesProvider.getSourceImage();
                    NativeCore.copyExportImageToBitmap(scaledWidth, scaledHeight, exportedBitmap);
                }
                NativeCore.deallocateExportImage();
                exportTilesProvider.cleanup();
                TilesProvider.cleanTexturesToClean();
                NativeCore.deactivateOffScreenFilter();
                NativeCore.INSTANCE.activateOnScreenFilterChecked(EmptyFilterParameter.INSTANCE);
                endOffscreenRendering(tilesProvider, filterType);
                NativeCore.resetRenderScaleMode();
                if (pause) {
                    onRenderListener = listener;
                    ImageViewGL.this.post(new Runnable() {
                        public void run() {
                            onRenderListener.onRenderCancelled();
                        }
                    });
                    return;
                }
                ImageViewGL.this.didApplyImage = true;
                this.needsEmptyFilterFrame = true;
                this.renderLoopCount = 2;
                final Bitmap bitmap = exportedBitmap;
                onRenderListener = listener;
                ImageViewGL.this.post(new Runnable() {
                    public void run() {
                        onRenderListener.onRenderFinished(bitmap);
                    }
                });
            }
        }

        public void renderImageArea(TilesProvider tilesProvider, Rect sourceRect, FilterParameter filter, final OnRenderListener listener) {
            ImageViewGL.this.post(new Runnable() {
                public void run() {
                    listener.onRenderProgressUpdate(0, 1);
                }
            });
            final Bitmap bitmap = Bitmap.createBitmap(sourceRect.width(), sourceRect.height(), Config.ARGB_8888);
            beginOffscreenRendering(tilesProvider, filter.getFilterType());
            NativeCore.offscreenFilterHundredPercentRegion(filter, tilesProvider.getSourceImage(), sourceRect.left, sourceRect.top, bitmap.getWidth(), bitmap.getHeight(), bitmap);
            endOffscreenRendering(tilesProvider, 13);
            ImageViewGL.this.post(new Runnable() {
                public void run() {
                    listener.onRenderProgressUpdate(1, 1);
                }
            });
            ImageViewGL.this.post(new Runnable() {
                public void run() {
                    listener.onRenderFinished(bitmap);
                }
            });
        }

        private void renderStyles(TilesProvider tilesProvider, int targetWidth, int targetHeight, FilterParameter filter, int parameter, OnBatchRenderListener listener) {
            if (!ImageViewGL.this.didApplyImage) {
                NativeCore.setRenderScaleMode(1);
                int[] values = filter.getParameterValues(parameter);
                beginOffscreenRendering(tilesProvider, filter.getFilterType());
                List<Bitmap> images = renderStyles(tilesProvider, targetWidth, targetHeight, filter, parameter, values, listener);
                endOffscreenRendering(tilesProvider, filter.getFilterType());
                NativeCore.resetRenderScaleMode();
                final List<Bitmap> sendImages = images;
                final OnBatchRenderListener onBatchRenderListener = listener;
                ImageViewGL.this.post(new Runnable() {
                    public void run() {
                        onBatchRenderListener.onRenderFinished(sendImages);
                    }
                });
            }
        }

        private List<Bitmap> renderStyles(TilesProvider tilesProvider, int targetWidth, int targetHeight, FilterParameter filter, int parameter, int[] values, OnBatchRenderListener listener) {
            List<Bitmap> images = new ArrayList(values.length);
            ByteBuffer buffer = ByteBuffer.allocate((targetWidth * targetHeight) * 4);
            if (values.length >= 2) {
                filter.setParameterValueOld(parameter, values[1]);
            }
            int styleCount = values.length;
            final OnBatchRenderListener onBatchRenderListener = listener;
            final int i = styleCount;
            ImageViewGL.this.post(new Runnable() {
                public void run() {
                    onBatchRenderListener.onRenderProgressUpdate(0, i);
                }
            });
            int current = 0;
            for (int value : values) {
                images.add(renderStyleForValue(tilesProvider, filter, parameter, value, targetWidth, targetHeight, buffer));
                current++;
                onBatchRenderListener = listener;
                i = current;
                final int i2 = styleCount;
                ImageViewGL.this.post(new Runnable() {
                    public void run() {
                        onBatchRenderListener.onRenderProgressUpdate(i, i2);
                    }
                });
            }
            buffer.clear();
            return images;
        }

        private Bitmap renderStyleForValue(TilesProvider tilesProvider, FilterParameter params, int filterParameterType, int value, int targetWidth, int targetHeight, ByteBuffer buffer) {
            params.setParameterValueOld(filterParameterType, value);
            NativeCore.contextAction(params, 6);
            NativeCore.offscreenFilterPreviewToBuffer(params, tilesProvider, targetWidth, targetHeight, buffer.array());
            Bitmap bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Config.ARGB_8888);
            try {
                buffer.rewind();
                bitmap.copyPixelsFromBuffer(buffer);
            } catch (Exception e) {
                bitmap.eraseColor(-65536);
            }
            return bitmap;
        }

        private void beginOffscreenRendering(TilesProvider tilesProvider, int filterType) {
            if (filterType == 7 || filterType == 3) {
                NativeCore.deactivateOffScreenFilter();
            }
            storeEGL();
            tilesProvider.lock();
            NativeCore.initOffscreenContext();
            NativeCore.activateOffScreenFilter(filterType);
            NativeCore.offscreenContextMakeCurrent();
        }

        private void endOffscreenRendering(TilesProvider tilesProvider, int filterType) {
            if (filterType == 7 || filterType == 3) {
                NativeCore.deactivateOffScreenFilter();
            }
            tilesProvider.unlock();
            restoreEGL();
        }

        private void storeEGL() {
            EGL10 egl = (EGL10) EGLContext.getEGL();
            this.currentContext = egl.eglGetCurrentContext();
            this.currentSurface = egl.eglGetCurrentSurface(12377);
            this.currentDisplay = egl.eglGetCurrentDisplay();
            if (this.currentContext == EGL10.EGL_NO_CONTEXT || this.currentSurface == EGL10.EGL_NO_SURFACE || this.currentDisplay == EGL10.EGL_NO_DISPLAY) {
                throw new IllegalStateException("Failed to store the EGL context");
            }
        }

        private void restoreEGL() {
            if (!((EGL10) EGLContext.getEGL()).eglMakeCurrent(this.currentDisplay, this.currentSurface, this.currentSurface, this.currentContext)) {
                Log.e(ImageViewGL.LOG_TAG, String.format("ImageViewGL restoreEGL failed: %d", new Object[]{Integer.valueOf(egl.eglGetError())}));
                throw new IllegalStateException("Failed to restore the EGL context");
            }
        }

        private void handleException(Exception ex) {
            if (ex.toString() != null) {
                Log.e(ImageViewGL.LOG_TAG, ex.toString());
            }
            if (ex.getMessage() != null) {
                Log.e(ImageViewGL.LOG_TAG, ex.getMessage());
            }
            if (ex.getLocalizedMessage() != null) {
                Log.e(ImageViewGL.LOG_TAG, ex.getLocalizedMessage());
            }
        }

        public void cleanupNativeCore(boolean restoreEGL) {
            if (restoreEGL) {
                storeEGL();
            }
            NativeCore.deactivateOnScreenFilter();
            NativeCore.deactivateOffScreenFilter();
            NativeCore.deleteOffscreenContext();
            if (restoreEGL) {
                restoreEGL();
            }
            this.needsEmptyFilterFrame = true;
            this.renderLoopCount = 2;
        }
    }

    private class StylesEvent implements Runnable {
        private final FilterParameter filter;
        private final int height;
        private final OnBatchRenderListener listener;
        private final int parameter;
        private final TilesProvider tilesProvider;
        private final int width;

        public StylesEvent(TilesProvider tilesProvider, int width, int height, FilterParameter filter, int parameter, OnBatchRenderListener listener) {
            this.tilesProvider = tilesProvider;
            this.width = width;
            this.height = height;
            this.filter = filter;
            this.parameter = parameter;
            this.listener = listener;
        }

        public void run() {
            try {
                ImageViewGL.this.renderer.renderStyles(this.tilesProvider, this.width, this.height, this.filter, this.parameter, this.listener);
            } catch (Exception exception) {
                ImageViewGL.this.renderer.handleException(exception);
            }
        }
    }

    static /* synthetic */ int access$404(ImageViewGL x0) {
        int i = x0.renderImageQueueSize + 1;
        x0.renderImageQueueSize = i;
        return i;
    }

    static /* synthetic */ int access$406(ImageViewGL x0) {
        int i = x0.renderImageQueueSize - 1;
        x0.renderImageQueueSize = i;
        return i;
    }

    public ImageViewGL(Context context) {
        super(context);
        SurfaceHolder surfaceHolder = getHolder();
        if (surfaceHolder == null) {
            throw new IllegalStateException("Failed to get the surface holder");
        }
        surfaceHolder.setFormat(3);
        setEGLContextClientVersion(2);
        setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        this.renderer = new Renderer();
        setRenderer(this.renderer);
        setRenderMode(0);
        NativeCore.setDefaultRenderScaleMode(2);
        NativeCore.resetRenderScaleMode();
    }

    public synchronized void setGeometryObjects(ArrayList<GeometryObject> objects) {
        if (this.renderer != null) {
            this.renderer.setGeometryObjects(objects);
        }
    }

    public void setFpsListener(FpsListener fpsListener, int updateIntervalMsec, boolean useContinuousRendering) {
        if (this.renderer != null) {
            this.renderer.setFpsListener(fpsListener, updateIntervalMsec, useContinuousRendering);
        }
    }

    public Hashtable<String, String> getGLProperties() {
        return this.glProperties;
    }

    public void setLifecycleListener(RendererLifecycleListener listener) {
        this.lifecycleListener = listener;
    }

    public boolean requestRenderImage(DataSource dataSource, Rect sourceRect, FilterParameter filter, OnRenderListener listener, boolean forceNoRequestStacking) {
        if (listener == null) {
            throw new NullPointerException("Listener cannot be null");
        } else if (forceNoRequestStacking && this.renderImageQueueSize > 0) {
            return false;
        } else {
            try {
                queueEvent(new ExportEvent((TilesProvider) dataSource, sourceRect, sourceRect != null ? filter : filter.clone(), listener));
                return true;
            } catch (CloneNotSupportedException e) {
                return false;
            }
        }
    }

    public boolean requestRenderStyleImages(DataSource dataSource, int width, int height, FilterParameter filter, int parameter, OnBatchRenderListener listener) {
        if (listener == null) {
            throw new NullPointerException("Listener cannot be null");
        }
        try {
            queueEvent(new StylesEvent((TilesProvider) dataSource, width, height, filter.clone(), parameter, listener));
            return true;
        } catch (CloneNotSupportedException e) {
            return false;
        }
    }

    public void setPreviewFilterParameter(FilterParameter filter) {
        this.previewFilter = filter;
    }

    public void setPreviewDataSource(DataSource dataSource) {
        this.previewTilesProvider = (TilesProvider) dataSource;
        NativeCore.INSTANCE.setTilesProvider(this.previewTilesProvider);
    }

    public boolean requestRenderPreview() {
        if (this.didApplyImage || !this.contextIsInitialized) {
            return false;
        }
        super.requestRender();
        return true;
    }

    private synchronized void setShouldRenderHighResolution() {
        this.highResolutionUpdate = true;
    }

    public synchronized boolean shouldRenderHighResolution(int filterType) {
        boolean z;
        z = this.highResolutionUpdate || NativeCore.INSTANCE.getCompare() || !needsHighResolutionUpdate(filterType);
        return z;
    }

    private boolean needsHighResolutionUpdate(int filterType) {
        return filterType == 3 || filterType == 7;
    }

    public synchronized void resetFirstFrame() {
        this.didApplyImage = false;
    }

    private synchronized void onFrame(boolean wasHighRes, int filterType) {
        if (!this.singleShotOnPreviewListeners.isEmpty()) {
            for (OnPreviewRenderListener listener : this.singleShotOnPreviewListeners) {
                postOnPreviewRendered(listener);
            }
            this.singleShotOnPreviewListeners.clear();
        }
        if (!this.permanentOnPreviewListeners.isEmpty()) {
            for (OnPreviewRenderListener listener2 : this.permanentOnPreviewListeners) {
                postOnPreviewRendered(listener2);
            }
        }
        if (!wasHighRes && needsHighResolutionUpdate(filterType)) {
            removeCallbacks(this.delayedUpdate);
            postDelayed(this.delayedUpdate, 200);
        }
        this.highResolutionUpdate = false;
        NotificationCenter.getInstance().performAction(ListenerType.DidRenderFrame, null);
    }

    private void postOnPreviewRendered(final OnPreviewRenderListener listener) {
        post(new Runnable() {
            public void run() {
                listener.onPreviewRendered();
            }
        });
    }

    public void surfaceCreated(SurfaceHolder holder) {
        super.surfaceCreated(holder);
        this.contextIsInitialized = true;
        NativeCore.INSTANCE.setPreviewRenderer(this);
        NativeCore.INSTANCE.setNotificationListener(new NativeCoreNotificationListener() {
            public void onInitializedOnScreenFilter() {
                if (ImageViewGL.this.lifecycleListener != null) {
                    ImageViewGL.this.lifecycleListener.onRendererInit();
                }
            }
        });
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        this.contextIsInitialized = false;
        NativeCore.INSTANCE.setPreviewRenderer(null);
        NativeCore.INSTANCE.setTilesProvider(null);
        removeAllOnPreviewRenderedListeners();
        this.renderer.pendingExportListener = null;
        this.renderer.pendingExportTilesProvider = null;
        this.renderer.pendingExportFilter = null;
        this.renderImageQueueSize = 0;
        if (this.previewTilesProvider != null) {
            this.previewTilesProvider.cleanupGL();
            this.previewTilesProvider = null;
        }
        this.previewFilter = null;
        queueEvent(new CleanupNativeCoreEvent());
        if (this.lifecycleListener != null) {
            this.lifecycleListener.onRendererCleanUp();
        }
        super.surfaceDestroyed(holder);
    }

    public synchronized boolean getPaused() {
        return this.breakProcess;
    }

    public synchronized void setPaused() {
        this.breakProcess = true;
    }

    public void addOnPreviewRenderedListener(OnPreviewRenderListener listener, boolean singleShot) {
        if (singleShot) {
            this.singleShotOnPreviewListeners.add(listener);
        } else {
            this.permanentOnPreviewListeners.add(listener);
        }
    }

    public void removeOnPreviewRenderedListener(OnPreviewRenderListener listener) {
        this.singleShotOnPreviewListeners.remove(listener);
        this.permanentOnPreviewListeners.remove(listener);
    }

    public void removeAllOnPreviewRenderedListeners() {
        this.singleShotOnPreviewListeners.clear();
        this.permanentOnPreviewListeners.clear();
    }
}

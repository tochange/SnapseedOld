package com.niksoftware.snapseed.core;

import android.graphics.Bitmap;
import android.graphics.Rect;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import java.util.List;

public final class RenderFilterInterface {

    public interface OnPreviewRenderListener {
        void onPreviewRendered();
    }

    public interface OnRenderListener {
        void onRenderCancelled();

        void onRenderFinished(Bitmap bitmap);

        void onRenderProgressUpdate(int i, int i2);
    }

    public interface OnBatchRenderListener {
        void onRenderCancelled();

        void onRenderFinished(List<Bitmap> list);

        void onRenderProgressUpdate(int i, int i2);
    }

    public interface DataSource {
    }

    public interface ImageRenderer {
        boolean requestRenderImage(DataSource dataSource, Rect rect, FilterParameter filterParameter, OnRenderListener onRenderListener, boolean z);
    }

    public interface PreviewRenderer {
        boolean requestRenderPreview();

        void setPreviewDataSource(DataSource dataSource);

        void setPreviewFilterParameter(FilterParameter filterParameter);
    }

    public interface RendererLifecycleListener {
        void onRendererCleanUp();

        void onRendererInit();
    }

    public interface StyleRenderer {
        boolean requestRenderStyleImages(DataSource dataSource, int i, int i2, FilterParameter filterParameter, int i3, OnBatchRenderListener onBatchRenderListener);
    }
}

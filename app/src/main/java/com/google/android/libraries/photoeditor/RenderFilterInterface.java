package com.google.android.libraries.photoeditor;

import android.graphics.Bitmap;
import android.graphics.Rect;
import com.google.android.libraries.photoeditor.filterparameters.FilterParameter;
import java.util.List;

public final class RenderFilterInterface {

    public interface ImageRenderer {
        boolean requestRenderImage(DataSource dataSource, Rect rect, FilterParameter filterParameter, OnRenderListener onRenderListener, boolean z);
    }

    public interface StyleRenderer {
        boolean requestRenderStyleImages(DataSource dataSource, int i, int i2, FilterParameter filterParameter, int i3, OnBatchRenderListener onBatchRenderListener);
    }

    public interface PreviewRenderer {
        boolean requestRenderPreview();

        void setPreviewDataSource(DataSource dataSource);

        void setPreviewFilterParameter(FilterParameter filterParameter);
    }

    public interface DataSource {
    }

    public interface OnBatchRenderListener {
        void onRenderCancelled();

        void onRenderFinished(List<Bitmap> list);

        void onRenderProgressUpdate(int i, int i2);
    }

    public interface OnPreviewRenderListener {
        void onPreviewRendered();
    }

    public interface OnRenderListener {
        void onRenderCancelled();

        void onRenderFinished(Bitmap bitmap);

        void onRenderProgressUpdate(int i, int i2);
    }

    public interface RendererLifecycleListener {
        void onRendererCleanUp();

        void onRendererInit();
    }
}

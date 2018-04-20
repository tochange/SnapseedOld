package com.google.android.libraries.photoeditor;

import android.graphics.Bitmap;
import com.google.android.libraries.photoeditor.filterparameters.FilterParameter;

public class FilterChainNode {
    private final FilterParameter filterParameter;
    private Bitmap previewLayer = null;

    public FilterChainNode(FilterParameter filterParameter) {
        this.filterParameter = filterParameter;
    }

    public FilterParameter getFilterParameter() {
        return this.filterParameter;
    }

    public Bitmap getPreviewLayer() {
        return this.previewLayer;
    }
}

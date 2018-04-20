package com.niksoftware.snapseed.core;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public final class DeviceDefs {
    static final /* synthetic */ boolean $assertionsDisabled = (!DeviceDefs.class.desiredAssertionStatus());
    private static boolean _isInitialized;
    private static boolean _isTablet;
    private static float _screenDensity;

    private DeviceDefs() {
    }

    public static void initialize(Context context, WindowManager windowManager) {
        _isTablet = context.getResources().getInteger(R.integer.is_tablet) != 0;
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        _screenDensity = metrics.density;
        _isInitialized = true;
    }

    public static boolean isTablet() {
        if ($assertionsDisabled || !_isInitialized) {
            return _isTablet;
        }
        throw new AssertionError("DeviceDefs.initialize() method needs to be called first");
    }

    public static float getScreenDensityRatio() {
        if ($assertionsDisabled || !_isInitialized) {
            return _screenDensity;
        }
        throw new AssertionError("DeviceDefs.initialize() method needs to be called first");
    }
}

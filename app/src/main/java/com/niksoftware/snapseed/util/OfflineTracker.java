package com.niksoftware.snapseed.util;

import android.util.Log;

public class OfflineTracker implements TrackerInterface {
    private static final String APP_NAME = "Snapseed";

    public void sendEvent(String category, String action, String label, long value) {
        logMessage(String.format("Tracked data - category: %s action: %s label: %s value: %d", new Object[]{category, action, label, Long.valueOf(value)}));
    }

    public void sendView(String view) {
        logMessage(String.format("Tracked data - view: %s", new Object[]{view}));
    }

    private static void logMessage(String message) {
        Log.i(APP_NAME, message);
    }
}

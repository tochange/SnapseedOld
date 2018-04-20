package com.niksoftware.snapseed.util;

import com.niksoftware.snapseed.R.drawable;
import com.niksoftware.snapseed.R.string;
import org.w3c.dom.DOMException;

public final class ContextHelper {
    public static final int INVALID_RESOURCE_ID = -1;

    private ContextHelper() {
    }

    public static int getDrawableResourceId(String resourceName) {
        int dotIndex = resourceName.indexOf(".");
        if (dotIndex > 0) {
            resourceName = resourceName.substring(0, dotIndex);
        }
        int resourceId = -1;
        try {
            resourceId = drawable.class.getDeclaredField(resourceName.toLowerCase()).getInt(null);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (DOMException e2) {
            e2.printStackTrace();
        } catch (NoSuchFieldException e3) {
            e3.printStackTrace();
        } catch (IllegalArgumentException e4) {
            e4.printStackTrace();
        } catch (IllegalAccessException e5) {
            e5.printStackTrace();
        }
        return resourceId;
    }

    public static int getStringResourceId(String resourceName) {
        int resourceId = -1;
        try {
            resourceId = string.class.getDeclaredField(resourceName).getInt(null);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (DOMException e2) {
            e2.printStackTrace();
        } catch (NoSuchFieldException e3) {
            e3.printStackTrace();
        } catch (IllegalArgumentException e4) {
            e4.printStackTrace();
        } catch (IllegalAccessException e5) {
            e5.printStackTrace();
        }
        return resourceId;
    }
}

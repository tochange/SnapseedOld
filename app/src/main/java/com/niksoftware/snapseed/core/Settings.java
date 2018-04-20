package com.niksoftware.snapseed.core;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.util.SparseArray;
import com.niksoftware.snapseed.core.FilterDefs.FilterType;
import java.lang.reflect.Field;

public class Settings {
    public static final String ENABLE_GOOGLE_ANALYTICS_KEY = "pref_trackEvents";
    private static final SparseArray<String> FILTER_TYPE_TO_STRING_MAP = new SparseArray();
    private static final String FIRST_RUN_KEY = "firstRun";
    private static final String NEEDS_MIGRATE_STORAGE_DIRECTORY = "needsMigrateStorage";
    private static final String PREFS_NAME = "SnapseedPrefs";

    static {
        for (Field field : FilterType.class.getDeclaredFields()) {
            try {
                FILTER_TYPE_TO_STRING_MAP.put(field.getInt(null), "showHelp_for_" + field.getName());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e2) {
            }
        }
    }

    private Settings() {
    }

    public static boolean getIsFirstRun(Context context) {
        return context.getSharedPreferences("SnapseedPrefs", 0).getBoolean(FIRST_RUN_KEY, true);
    }

    public static void setIsFirstRun(Context context, boolean isFirstRun) {
        Editor editor = context.getSharedPreferences("SnapseedPrefs", 0).edit();
        editor.putBoolean(FIRST_RUN_KEY, isFirstRun);
        editor.commit();
    }

    public static boolean getNeedsMigrateStorage(Context context) {
        return context.getSharedPreferences("SnapseedPrefs", 0).getBoolean(NEEDS_MIGRATE_STORAGE_DIRECTORY, true);
    }

    public static void setNeedsMigrateStorage(Context context, boolean needsMigrate) {
        Editor editor = context.getSharedPreferences("SnapseedPrefs", 0).edit();
        editor.putBoolean(NEEDS_MIGRATE_STORAGE_DIRECTORY, needsMigrate);
        editor.commit();
    }

    public static boolean shouldTrackEvents(Context context) {
        return false;
    }

    public static boolean getNeedsShowHelp(Context context, int filterType) {
        return context.getSharedPreferences("SnapseedPrefs", 0).getBoolean((String) FILTER_TYPE_TO_STRING_MAP.get(filterType), true);
    }

    public static void setNeedsShowHelp(Context context, int filterType, boolean showHelp) {
        String filterKey = (String) FILTER_TYPE_TO_STRING_MAP.get(filterType);
        Editor editor = context.getSharedPreferences("SnapseedPrefs", 0).edit();
        editor.putBoolean(filterKey, showHelp);
        editor.commit();
    }
}

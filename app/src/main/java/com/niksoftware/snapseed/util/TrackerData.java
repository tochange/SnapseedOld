package com.niksoftware.snapseed.util;

import android.text.TextUtils;
import android.util.Log;
import com.niksoftware.snapseed.core.FilterDefs.ContextAction;
import com.niksoftware.snapseed.core.FilterDefs.FilterParameterType;
import com.niksoftware.snapseed.core.FilterDefs.FilterType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TrackerData {
    public static final String UI_ACTION = "ui_action";
    public static final String VIEW_HELP_SCREEN = "helpscreen";
    public static final String VIEW_MAIN_SCREEN = "mainscreen";
    private static TrackerData instance;
    private final List<NameDurationPair> addedFilters = new ArrayList();
    private final List<NameDurationPair> canceledFilters = new ArrayList();
    private boolean isSampleImage = true;
    private TrackerInterface tracker;
    private final Map<Integer, Boolean> usedParameters = new HashMap();
    private final Map<String, UserAction> userActions = new HashMap();

    private static class NameDurationPair {
        private final long duration;
        private final String name;

        public NameDurationPair(String name, long duration) throws IllegalArgumentException {
            if (name == null) {
                throw new IllegalArgumentException("Name is null.");
            }
            this.name = name;
            this.duration = duration;
        }

        public String toString() {
            return this.name;
        }
    }

    private class UserAction {
        private final String action;
        private final String label;
        private final long value;

        public UserAction(String action, String label, long value) {
            this.action = action;
            this.label = label;
            this.value = value;
        }

        public void sendData(String category, TrackerInterface tracker) {
            tracker.sendEvent(category, this.action, this.label, this.value);
        }
    }

    private TrackerData(TrackerInterface tracker) {
        this.tracker = tracker;
    }

    public static TrackerData getInstance() {
        if (instance != null) {
            return instance;
        }
        throw new IllegalStateException("Please call first initialize.");
    }

    public static void initialize(TrackerInterface tracker) {
        if (instance == null) {
            instance = new TrackerData(tracker);
        } else {
            Log.w("Snapseed", "TrackerData seems to get initialized twice.");
        }
    }

    public void updateTracker(TrackerInterface tracker) {
        if (tracker != this.tracker) {
            this.tracker = tracker;
        }
    }

    public static TrackerData newInstanceForTest(TrackerInterface tracker) {
        return new TrackerData(tracker);
    }

    public void addFilter(String filterName, long duration) {
        String category = sendFilterEvent("applyfilter", filterName, duration, false);
        StringBuilder stringBuilder = new StringBuilder();
        for (Integer intValue : this.usedParameters.keySet()) {
            String isDefault = ((Boolean) this.usedParameters.get(Integer.valueOf(intValue.intValue()))).booleanValue() ? "[true]" : "[false]";
            stringBuilder.append(String.format("%s %s, ", new Object[]{FilterParameterType.getParameterName(intValue.intValue()), isDefault}));
        }
        sendEvent(category, filterName, stringBuilder.toString(), 0);
        for (UserAction ua : this.userActions.values()) {
            ua.sendData(category, this.tracker);
        }
        clearFilterRelatedData();
    }

    public void cancelFilter(String name, long duration) {
        sendFilterEvent("cancelfilter", name, duration, true);
        clearFilterRelatedData();
    }

    private String sendFilterEvent(String action, String filterName, long duration, boolean canceled) {
        String category = UI_ACTION;
        if (this.isSampleImage) {
            category = "[sample]" + category;
        }
        sendEvent(category, action, filterName, duration);
        (canceled ? this.canceledFilters : this.addedFilters).add(new NameDurationPair(filterName, duration));
        return category;
    }

    public void sendDataImagesSaved() {
        sendData("Save Image");
    }

    public void sendDataImagesShared(boolean googlePlusShared) {
        String category = "Share Image";
        if (googlePlusShared) {
            category = "Share Image (Google+)";
        }
        sendData(category);
    }

    private void sendData(String category) {
        if (this.isSampleImage) {
            category = "[sample]" + category;
        }
        NameDurationPair totalFilterTimePair = combineNameDurationPairList(this.addedFilters);
        String str = category;
        sendEvent(str, String.format("applied filters (%d)", new Object[]{Integer.valueOf(this.addedFilters.size())}), totalFilterTimePair.name, totalFilterTimePair.duration);
        if (this.canceledFilters.size() > 0) {
            NameDurationPair totalFilterTimePairCanceled = combineNameDurationPairList(this.canceledFilters);
            sendEvent(category, "canceled filters", totalFilterTimePairCanceled.name, totalFilterTimePairCanceled.duration);
        }
    }

    private static NameDurationPair combineNameDurationPairList(List<NameDurationPair> list) {
        long totalDuration = 0;
        String joinedNames = TextUtils.join(", ", list);
        for (NameDurationPair p : list) {
            totalDuration += p.duration;
        }
        return new NameDurationPair(joinedNames, totalDuration);
    }

    public void newSessionBecauseOfNewImage() {
        clearData();
        this.isSampleImage = false;
        sendEvent("load Image", "", "", 0);
    }

    public void revertImage() {
        NameDurationPair totalFilterTimePair = combineNameDurationPairList(this.addedFilters);
        sendEvent("revert Image", totalFilterTimePair.name, "", totalFilterTimePair.duration);
        clearData();
    }

    public void sendEvent(String category, String action, String label, long value) {
        if (this.tracker == null) {
            throw new IllegalStateException("Tracker not initialized");
        }
        this.tracker.sendEvent(category, action, label, value);
    }

    public void sendView(String view) {
        if (this.tracker == null) {
            throw new IllegalStateException("Tracker not initialized");
        }
        this.tracker.sendView(view);
    }

    private void clearData() {
        this.addedFilters.clear();
        this.canceledFilters.clear();
        clearFilterRelatedData();
    }

    private void clearFilterRelatedData() {
        this.userActions.clear();
        this.usedParameters.clear();
    }

    public void usingParameter(int type, boolean isDefault) {
        if (!this.usedParameters.containsKey(Integer.valueOf(type))) {
            this.usedParameters.put(Integer.valueOf(type), Boolean.valueOf(isDefault));
        }
    }

    public void randomizeAction(int filterId, int action) {
        String label = ContextAction.getNameFor(action);
        if (!this.userActions.containsKey(label)) {
            this.userActions.put(label, new UserAction(FilterType.getFilterName(filterId), label, 0));
        }
    }
}

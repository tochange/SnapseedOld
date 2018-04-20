package com.niksoftware.snapseed.controllers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ControllerContext {
    public static final String EDITING_TOOL_BAR = "editingToolBar";
    public static final String EDIT_SESSION = "editSession";
    public static final String ROOT_VIEW = "rootView";
    public static final String WORKING_AREA_VIEW = "workingAreaView";
    private final Map<String, Object> contextMap;

    public static class Builder {
        private final Map<String, Object> contextMap = new HashMap();

        public Builder put(String key, Object value) {
            this.contextMap.put(key, value);
            return this;
        }

        public ControllerContext createContext() {
            return new ControllerContext(this.contextMap);
        }
    }

    private ControllerContext(Map<String, Object> contextMap) {
        this.contextMap = Collections.unmodifiableMap(contextMap);
    }

    public Object get(String key) {
        return this.contextMap.get(key);
    }
}

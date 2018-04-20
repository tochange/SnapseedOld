package com.niksoftware.snapseed.core;

public interface NotificationCenterListener {

    public enum ListenerType {
        WillActivateFilter,
        DidActivateFilter,
        DidCreateFilterGUI,
        DidChangeActiveFilterParameter,
        UndoRedoStateChanged,
        UndoRedoPerformed,
        DidChangeFilterParameterValue,
        DidApplyFilter,
        WillApplyFilter,
        DidChangeCompareImageMode,
        DidChangeParameterViewVisibility,
        DidEnterMainScreen,
        DidEnterEditingScreen,
        DidRenderFrame
    }

    void performAction(Object obj);
}

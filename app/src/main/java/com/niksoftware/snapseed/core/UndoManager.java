package com.niksoftware.snapseed.core;

import android.content.res.Resources;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.core.filterparameters.UPointFilterParameter;
import com.niksoftware.snapseed.core.filterparameters.UPointParameter;
import com.niksoftware.snapseed.views.EditingToolBar;
import java.util.Stack;

public class UndoManager {
    private static final int MAX_UNDO_COUNT = 25;
    private static UndoManager _instance;
    private Stack<UndoObject> _redoStack = new Stack();
    private Stack<UndoObject> _undoStack = new Stack();

    private UndoManager() {
        this._undoStack.ensureCapacity(25);
        this._redoStack.ensureCapacity(25);
    }

    public static UndoManager getUndoManager() {
        if (_instance == null) {
            _instance = new UndoManager();
        }
        return _instance;
    }

    public void createUndo(FilterParameter param, int changedFilterParameterType) {
        pushUndo(new UndoObject(param, changedFilterParameterType, null));
    }

    public void createUndo(FilterParameter param, int changedFilterParameterType, boolean forceNoDescription) {
        pushUndo(new UndoObject(param, changedFilterParameterType, forceNoDescription));
    }

    public void createUndo(FilterParameter param, int changedFilterParameterType, String description) {
        pushUndo(new UndoObject(param, changedFilterParameterType, description));
    }

    public void pushUndo(UndoObject undo) {
        EditingToolBar editingToolBar = MainActivity.getEditingToolbar();
        if (editingToolBar != null) {
            editingToolBar.setUndoButtonSelectionState(true);
        }
        this._undoStack.push(undo);
        if (this._undoStack.size() > 25) {
            this._undoStack.remove(this._undoStack.firstElement());
        }
        this._redoStack.clear();
        NotificationCenter.getInstance().performAction(ListenerType.UndoRedoStateChanged, null);
    }

    public void clear() {
        EditingToolBar editingToolBar = MainActivity.getEditingToolbar();
        if (editingToolBar != null) {
            editingToolBar.setUndoButtonSelectionState(false);
        }
        this._undoStack.clear();
        this._redoStack.clear();
        NotificationCenter.getInstance().performAction(ListenerType.UndoRedoStateChanged, null);
    }

    public boolean canUndo() {
        return this._undoStack.size() > 0;
    }

    public boolean canRedo() {
        return this._redoStack.size() > 0;
    }

    private UndoObject fixUndoObject(FilterParameter filterParameter, UndoObject undoObject) {
        if (undoObject.forceNoDescription()) {
            return new UndoObject(filterParameter, undoObject.getChangedParameter(), true);
        }
        return new UndoObject(filterParameter, undoObject.getChangedParameter(), undoObject.hasStaticDescription() ? undoObject.getDescription(MainActivity.getMainActivity()) : null);
    }

    private void makeUndoRedo(UndoReceiver receiver, boolean isUndo) {
        UndoObject undoObject = (UndoObject) (isUndo ? this._undoStack : this._redoStack).pop();
        UndoObject redo = fixUndoObject(receiver.getFilterParameter(), undoObject);
        FilterParameter undoFilter = undoObject.getFilterParameter();
        if (undoFilter instanceof UPointFilterParameter) {
            Resources resources = MainActivity.getMainActivity().getResources();
            String oldDescription = undoObject.getDescription(MainActivity.getMainActivity());
            boolean isAdd = oldDescription.equals(resources.getString(R.string.undo_upoint_add)) || oldDescription.equals(resources.getString(R.string.undo_upoint_paste)) || oldDescription.equals(resources.getString(R.string.undo_upoint_cut)) || oldDescription.equals(resources.getString(R.string.undo_upoint_delete)) || oldDescription.equals(resources.getString(R.string.undo_upoint_move));
            if (!isAdd) {
                UPointParameter activeUPointParameter = ((UPointFilterParameter) undoFilter).getActiveUPoint();
                UPointParameter undoUPointParameter = ((UPointFilterParameter) redo.getFilterParameter()).getActiveUPoint();
                if (!(activeUPointParameter == null || undoUPointParameter == null)) {
                    undoUPointParameter.setActiveFilterParameter(activeUPointParameter.getActiveFilterParameter());
                }
            }
        } else {
            redo.getFilterParameter().setActiveFilterParameter(undoFilter.getActiveFilterParameter());
        }
        (isUndo ? this._redoStack : this._undoStack).push(redo);
        receiver.makeUndo(undoObject);
        NotificationCenter notificationCenter = NotificationCenter.getInstance();
        notificationCenter.performAction(ListenerType.UndoRedoPerformed, undoObject);
        notificationCenter.performAction(ListenerType.UndoRedoStateChanged, undoObject);
    }

    public void makeUndo(UndoReceiver receiver) {
        if (canUndo()) {
            makeUndoRedo(receiver, true);
        }
    }

    public void makeRedo(UndoReceiver receiver) {
        if (canRedo()) {
            makeUndoRedo(receiver, false);
        }
    }
}

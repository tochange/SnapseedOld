package com.niksoftware.snapseed.core;

import com.niksoftware.snapseed.core.filterparameters.FilterParameter;

public interface UndoReceiver {
    FilterParameter getFilterParameter();

    void makeRedo(UndoObject undoObject);

    void makeUndo(UndoObject undoObject);
}

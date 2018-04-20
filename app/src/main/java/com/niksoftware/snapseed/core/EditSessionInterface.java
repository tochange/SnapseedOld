package com.niksoftware.snapseed.core;

import android.net.Uri;

public interface EditSessionInterface {

    public interface SaveImageListener {
        void onSaveFinished(EditSession editSession, OperationResult operationResult, Uri uri);

        void onSaveStarted(EditSession editSession);
    }

    public interface LoadImageListener {
        void onLoadFinished(EditSession editSession, OperationResult operationResult, int i, int i2);

        void onLoadStarted(EditSession editSession);
    }

    public interface RevertImageListener {
        void onRevertFinished(EditSession editSession, OperationResult operationResult);

        void onRevertStarted(EditSession editSession);
    }

    public interface RemoveFilterListener {
        void onRemoveFilterFinished(EditSession editSession, OperationResult operationResult);

        void onRemoveFilterStarted(EditSession editSession);
    }

    public enum OperationResult {
        SUCCESS,
        ERROR_COULD_NOT_OPEN,
        ERROR_COULD_NOT_DECODE,
        ERROR_EXTREME_ASPECT_RATIO,
        WARNING_NOTHING_TO_REVERT,
        ERROR_NOT_INITIALIZED,
        ERROR_SAVE_FAILED,
        ERROR_SAVE_SD_NOT_WRITABLE,
        ERROR_REMOVE_LAST_FILTER_FAILED
    }
}

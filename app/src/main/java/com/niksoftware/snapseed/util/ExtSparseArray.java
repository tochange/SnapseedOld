package com.niksoftware.snapseed.util;

import android.util.SparseArray;

public class ExtSparseArray<E> extends SparseArray<E> {
    public static final int INDEX_NOT_FOUND = -1;

    public ExtSparseArray(int capacity) {
        super(capacity);
    }

    public int indexOfValue(E value) {
        int size = size();
        for (int i = 0; i < size; i++) {
            if (value.equals(valueAt(i))) {
                return i;
            }
        }
        return -1;
    }
}

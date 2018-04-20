package com.niksoftware.snapseed.controllers;

public class AutoshuffleFilterController extends EmptyFilterController {
    private boolean forceNoShuffle;

    public void onOnScreenFilterCreated(int filterType) {
        super.onOnScreenFilterCreated(filterType);
        if (!this.forceNoShuffle && filterType == getFilterType()) {
            randomize(false);
            this.forceNoShuffle = true;
        }
    }
}

package com.niksoftware.snapseed.controllers;

public class HdrController extends DetailsController {
    public int getFilterType() {
        return 15;
    }

    public int[] getGlobalAdjustmentParameters() {
        return new int[]{12, 1, 2, 231};
    }

    public int getHelpResourceId() {
        return R.xml.overlay_details;
    }
}

package com.niksoftware.snapseed.controllers;

public class AutoCorrectController extends EmptyFilterController {
    private static final int[] ADJUSTABLE_FILTER_PARAMETERS = new int[]{7, 8};

    public void init(ControllerContext context) {
        super.init(context);
        addParameterHandler();
    }

    public int getFilterType() {
        return 2;
    }

    public int[] getGlobalAdjustmentParameters() {
        return ADJUSTABLE_FILTER_PARAMETERS;
    }

    public boolean showsParameterView() {
        return true;
    }
}

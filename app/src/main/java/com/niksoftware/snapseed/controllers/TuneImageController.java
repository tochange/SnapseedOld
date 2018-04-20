package com.niksoftware.snapseed.controllers;

public class TuneImageController extends EmptyFilterController {
    private static final int[] ADJUSTABLE_FILTER_PARAMETERS = new int[]{0, 10, 1, 2, 20, 11};

    public void init(ControllerContext context) {
        super.init(context);
        addParameterHandler();
    }

    public int getFilterType() {
        return 4;
    }

    public int[] getGlobalAdjustmentParameters() {
        return ADJUSTABLE_FILTER_PARAMETERS;
    }

    public boolean showsParameterView() {
        return true;
    }
}

package com.niksoftware.snapseed.controllers;

public class AutoEnhanceController extends EmptyFilterController {
    private static final int[] GLOBAL_ADJUSTMENT_PARAMETERS = new int[]{0, 1, 2};

    public void init(ControllerContext context) {
        super.init(context);
        addParameterHandler();
    }

    public int getFilterType() {
        return 4;
    }

    public int[] getGlobalAdjustmentParameters() {
        return GLOBAL_ADJUSTMENT_PARAMETERS;
    }

    public boolean showsParameterView() {
        return true;
    }
}

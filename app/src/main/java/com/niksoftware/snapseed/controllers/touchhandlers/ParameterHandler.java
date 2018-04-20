package com.niksoftware.snapseed.controllers.touchhandlers;

import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.views.ActiveParameterView;
import com.niksoftware.snapseed.views.ParameterView;
import com.niksoftware.snapseed.views.WorkingAreaView;

public class ParameterHandler extends ParameterHandlerBase {
    private static final float STYLE_TOUCH_RANGE_FACTOR = 10.0f;

    public void showParameterView(boolean visible) {
        WorkingAreaView workingAreaView = MainActivity.getWorkingAreaView();
        ParameterView parameterView = workingAreaView.getParameterView();
        if (parameterView != null) {
            int visibility = visible ? 0 : 8;
            if (parameterView.getVisibility() != visibility) {
                workingAreaView.requestLayout();
                if (visible) {
                    workingAreaView.getActionView().hide(false);
                    parameterView.setActiveParameterIndex(workingAreaView.getFilterParameter().getActiveFilterParameter());
                }
                parameterView.setVisibility(visibility);
                ActiveParameterView activeParameterView = workingAreaView.getActiveParameterView();
                activeParameterView.setVisibility(visibility);
                activeParameterView.bringToFront();
                NotificationCenter.getInstance().performAction(ListenerType.DidChangeParameterViewVisibility, Boolean.valueOf(visible));
            }
        }
    }

    private float getTouchRangeFactor(FilterParameter filter, int activeFilterParameter) {
        float rangeFactor = 750.0f / ((float) (filter.getMaxValue(activeFilterParameter) - filter.getMinValue(activeFilterParameter)));
        int filterType = filter.getFilterType();
        if ((filterType == 10 || filterType == 18) && activeFilterParameter == 3) {
            return rangeFactor * STYLE_TOUCH_RANGE_FACTOR;
        }
        return rangeFactor;
    }

    public boolean onChangeValue(float delta) {
        FilterParameter filter = MainActivity.getMainActivity().getFilterController().getFilterParameter();
        int activeFilterParameter = filter.getActiveFilterParameter();
        boolean needUpdate = filter.setParameterValueOld(activeFilterParameter, filter.getParameterValueOld(activeFilterParameter) + Math.round(delta / getTouchRangeFactor(filter, activeFilterParameter)));
        if (needUpdate) {
            TrackerData.getInstance().usingParameter(activeFilterParameter, filter.getDefaultParameter() == activeFilterParameter);
            NotificationCenter.getInstance().performAction(ListenerType.DidChangeFilterParameterValue, Integer.valueOf(filter.getActiveFilterParameter()));
            MainActivity.getWorkingAreaView().requestRender();
        }
        return needUpdate;
    }

    public void updateParameterMenu(int yOffset) {
        if (MainActivity.getWorkingAreaView().getParameterView() != null) {
            MainActivity.getWorkingAreaView().getParameterView().updateParameterMenu(yOffset);
        }
    }
}

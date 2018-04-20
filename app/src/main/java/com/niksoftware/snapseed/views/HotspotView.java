package com.niksoftware.snapseed.views;

import android.widget.ImageView;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.R;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;

public class HotspotView extends ImageView {
    public HotspotView() {
        super(MainActivity.getMainActivity());
        setImageDrawable(ParameterViewHelper.getDrawableFromResources(R.drawable.gfx_ct_centerpoint));
        measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
    }

    public double getCenterX(int parentWidth, int parentHeight) {
        return FilterParameter.lowBitsAsDouble(MainActivity.getFilterParameter().getParameterValueOld(24));
    }

    public double getCenterY(int parentWidth, int parentHeight) {
        return FilterParameter.lowBitsAsDouble(MainActivity.getFilterParameter().getParameterValueOld(25));
    }
}

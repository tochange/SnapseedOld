package com.niksoftware.snapseed.views;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.widget.LinearLayout;

import com.niksoftware.snapseed.R;
import com.niksoftware.snapseed.core.DeviceDefs;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GlobalToolBar extends LinearLayout {
    private static final int[] BUTTON_ICONS = new int[]{R.drawable.icon_darkbg_compare_default, R.drawable.icon_darkbg_revert_default, R.drawable.icon_save_default, R.drawable.icon_sharegplus_default};
    private static final int[] BUTTON_TITLE_IDS = new int[]{R.string.compare_btn, R.string.revert_btn, R.string.save_btn, R.string.google_plus_btn};
    private static final int DISABLED_BUTTON_TINT_COLOR = -7829368;
    private static final int SELECTED_BUTTON_TINT_COLOR = -8421505;
    private final List<ToolButton> buttons;
    protected final boolean isTablet = DeviceDefs.isTablet();

    public GlobalToolBar(Context context) {
        super(context);
        Resources resources = getResources();
        int titleSpacing = resources.getDimensionPixelSize(R.dimen.gb_button_title_spacing);
        List<ToolButton> toolButtons = new ArrayList();
        int i = 0;
        while (i < BUTTON_ICONS.length) {
            ToolButton button = new ToolButton(getContext());
            button.setLongClickable(false);
            if (this.isTablet || BUTTON_ICONS[i] != R.drawable.icon_save_default) {
                button.setStyleNoShadow(R.style.GlobalToolbarButtonTitle);
                button.setText(BUTTON_TITLE_IDS[i]);
                button.setStateImagesTintColor(BUTTON_ICONS[i], (int) SELECTED_BUTTON_TINT_COLOR, (int) DISABLED_BUTTON_TINT_COLOR);
                LayoutParams layoutParams = new LayoutParams(-2, -2);
                if (this.isTablet) {
                    button.setTitleSpacing(titleSpacing);
                    button.setTitleVisible(true);
                    button.setMinimumWidth(resources.getDimensionPixelSize(R.dimen.gb_button_min_width));
                    int vertPadding = resources.getDimensionPixelSize(R.dimen.gb_button_vert_padding);
                    button.setPadding(0, vertPadding, 0, vertPadding);
                    layoutParams.gravity = 16;
                } else {
                    int minSize = getResources().getDimensionPixelSize(R.dimen.tmp_wa_tool_button_min_size);
                    button.setMinimumWidth(minSize);
                    button.setMinimumHeight(minSize);
                }
                button.setLayoutParams(layoutParams);
            }
            toolButtons.add(button);
            if (this.isTablet) {
                addView(button);
            }
            i++;
        }
        this.buttons = Collections.unmodifiableList(toolButtons);
        setMinimumHeight(resources.getDimensionPixelSize(R.dimen.gb_min_height));
        setLayoutParams(new LayoutParams(-2, -2));
    }

    public void refreshButtonTitles() {
        for (int i = 0; i < this.buttons.size(); i++) {
            ((ToolButton) this.buttons.get(i)).setText(BUTTON_TITLE_IDS[i]);
        }
    }

    public View getCompareButton() {
        return (View) this.buttons.get(0);
    }

    public ToolButton getRevertButton() {
        return (ToolButton) this.buttons.get(1);
    }

    public ToolButton getSaveButton() {
        return (ToolButton) this.buttons.get(2);
    }

    public ToolButton getShareButton() {
        return (ToolButton) this.buttons.get(3);
    }
}

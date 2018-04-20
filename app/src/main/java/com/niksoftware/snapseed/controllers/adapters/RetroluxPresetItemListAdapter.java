package com.niksoftware.snapseed.controllers.adapters;

import android.content.Context;
import android.content.res.Resources;

import com.niksoftware.snapseed.core.NativeCore;
import com.niksoftware.snapseed.views.ItemSelectorView.ListAdapter;
import java.util.Locale;

public class RetroluxPresetItemListAdapter implements ListAdapter {
    private static final int LEAK_ITEM_ID_BASE = 200;
    private static final int SCRATCH_ITEM_ID_BASE = 100;
    private Integer _activeLeakIndex;
    private Integer _activeScratchIndex;
    private int[] _leakPreviewResIds;
    private String _leakTitleFormat;
    private int[] _leaks;
    private int[] _scratchPreviewResIds;
    private String _scratchTitleFormat;
    private int[] _scratches;

    public RetroluxPresetItemListAdapter(Context context, int styleId) {
        int i;
        NativeCore.getInstance();
        switch (NativeCore.retroluxGetScratchType(styleId)) {
            case 0:
                this._scratchTitleFormat = context.getString(R.string.fine) + " %d";
                break;
            case 1:
                this._scratchTitleFormat = context.getString(R.string.soft) + " %d";
                break;
            case 2:
                this._scratchTitleFormat = context.getString(R.string.dirt) + " %d";
                break;
            default:
                this._scratchTitleFormat = "*UNKNOWN* %d";
                break;
        }
        NativeCore.getInstance();
        switch (NativeCore.retroluxGetLeakType(styleId)) {
            case 0:
                this._leakTitleFormat = context.getString(R.string.soft) + " %d";
                break;
            case 1:
                this._leakTitleFormat = context.getString(R.string.dynamic) + " %d";
                break;
            case 2:
                this._leakTitleFormat = context.getString(R.string.crisp) + " %d";
                break;
            default:
                this._leakTitleFormat = "*UNKNOWN* %d";
                break;
        }
        Resources resources = context.getResources();
        String packageName = context.getPackageName();
        NativeCore.getInstance();
        this._scratches = NativeCore.retroluxGetScratches(styleId);
        this._scratchPreviewResIds = new int[(this._scratches.length * 2)];
        for (i = 0; i < this._scratches.length; i++) {
            this._scratchPreviewResIds[i * 2] = resources.getIdentifier(String.format(Locale.US, "icon_fo_retrolux_texture_%d_default", new Object[]{Integer.valueOf(this._scratches[i])}), "drawable", packageName);
            this._scratchPreviewResIds[(i * 2) + 1] = resources.getIdentifier(String.format(Locale.US, "icon_fo_retrolux_texture_%d_active", new Object[]{Integer.valueOf(this._scratches[i])}), "drawable", packageName);
        }
        NativeCore.getInstance();
        this._leaks = NativeCore.retroluxGetLeaks(styleId);
        this._leakPreviewResIds = new int[((this._leaks.length + 1) * 2)];
        this._leakPreviewResIds[0] = R.drawable.icon_fo_none_default;
        this._leakPreviewResIds[1] = R.drawable.icon_fo_none_active;
        for (i = 1; i <= this._leaks.length; i++) {
            this._leakPreviewResIds[i * 2] = resources.getIdentifier(String.format(Locale.US, "icon_fo_retrolux_lightleak_%02d_default", new Object[]{Integer.valueOf(this._leaks[i - 1])}), "drawable", packageName);
            this._leakPreviewResIds[(i * 2) + 1] = resources.getIdentifier(String.format(Locale.US, "icon_fo_retrolux_lightleak_%02d_active", new Object[]{Integer.valueOf(this._leaks[i - 1])}), "drawable", packageName);
        }
        this._activeScratchIndex = Integer.valueOf(0);
        this._activeLeakIndex = Integer.valueOf(0);
    }

    public void setActiveItems(int scratchIndex, int leakIndex) {
        this._activeScratchIndex = Integer.valueOf(scratchIndex);
        this._activeLeakIndex = Integer.valueOf(leakIndex);
    }

    public int getActiveScratchIndex() {
        return this._activeScratchIndex.intValue();
    }

    public int getActiveLeakIndex() {
        return this._activeLeakIndex.intValue();
    }

    public boolean isLeakItem(Integer itemId) {
        return itemId != null && itemId.intValue() >= 200;
    }

    public void setActiveItem(Integer itemId) {
        if (itemId != null) {
            if (itemId.intValue() < 200) {
                this._activeScratchIndex = Integer.valueOf(itemId.intValue() - 100);
            } else {
                this._activeLeakIndex = Integer.valueOf(itemId.intValue() - 200);
            }
        }
    }

    public int getItemCount() {
        return (this._scratches.length + this._leaks.length) + 2;
    }

    public Integer getItemId(int itemIndex) {
        if (itemIndex == this._scratches.length) {
            return null;
        }
        return Integer.valueOf(itemIndex < this._scratches.length ? itemIndex + 100 : ((itemIndex + 200) - this._scratches.length) - 1);
    }

    public Object[] getItemStateImages(Context context, Integer itemId) {
        if (itemId == null) {
            return null;
        }
        if (itemId.intValue() < 200) {
            int index = itemId.intValue() - 100;
            return new Integer[]{Integer.valueOf(this._scratchPreviewResIds[index * 2]), Integer.valueOf(this._scratchPreviewResIds[(index * 2) + 1])};
        }
        index = itemId.intValue() - 200;
        return new Integer[]{Integer.valueOf(this._leakPreviewResIds[index * 2]), Integer.valueOf(this._leakPreviewResIds[(index * 2) + 1])};
    }

    public String getItemText(Context context, Integer itemId) {
        if (itemId == null) {
            return null;
        }
        if (itemId.intValue() < 200) {
            return String.format(this._scratchTitleFormat, new Object[]{Integer.valueOf(itemId.intValue() - 100)});
        }
        String itemText;
        if (itemId.intValue() == 200) {
            itemText = context.getString(R.string.no_leak);
        } else {
            itemText = String.format(this._leakTitleFormat, new Object[]{Integer.valueOf(itemId.intValue() - 200)});
        }
        return itemText;
    }

    public boolean isItemActive(Integer itemId) {
        return itemId != null && (itemId.intValue() >= 200 ? this._activeLeakIndex.equals(Integer.valueOf(itemId.intValue() - 200)) : this._activeScratchIndex.equals(Integer.valueOf(itemId.intValue() - 100)));
    }

    public boolean hasContextItem() {
        return true;
    }

    public int getContextButtonImageId() {
        return R.drawable.icon_fo_back_default;
    }

    public String getContextButtonText(Context context) {
        return context.getString(R.string.back_label);
    }
}

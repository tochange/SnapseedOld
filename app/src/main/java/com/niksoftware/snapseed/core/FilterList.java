package com.niksoftware.snapseed.core;

import android.content.Context;
import android.view.View;

import com.niksoftware.snapseed.views.FilterListItemView;

public class FilterList {
    private static final FilterInfo[] FILTERS = new FilterInfo[]{new FilterInfo(2, (int) R.drawable.filtericons_automatic, (int) R.string.filtericon_automatik), new FilterInfo(3, (int) R.drawable.filtericons_selectiveadjust, (int) R.string.filtericon_local_adjust), new FilterInfo(4, (int) R.drawable.filtericons_tuneimage, (int) R.string.filtericon_tuneimage), new FilterInfo(5, (int) R.drawable.filtericons_straighten, (int) R.string.filtericon_rotate), new FilterInfo(6, (int) R.drawable.filtericons_crop, (int) R.string.filtericon_crop), new FilterInfo(13, (int) R.drawable.filtericons_details, (int) R.string.filtericon_details), new FilterInfo(7, (int) R.drawable.filtericons_blackandwhite, (int) R.string.filtericon_black_white), new FilterInfo(8, (int) R.drawable.filtericons_vintage, (int) R.string.filtericon_vintage), new FilterInfo(9, (int) R.drawable.filtericons_drama, (int) R.string.filtericon_drama), new FilterInfo(100, (int) R.drawable.filtericons_hdrscape, (int) R.string.filtericon_ambiance), new FilterInfo(10, (int) R.drawable.filtericons_grunge, (int) R.string.filtericon_grunge), new FilterInfo(11, (int) R.drawable.filtericons_centerfocus, (int) R.string.filtericon_center_focus), new FilterInfo(14, (int) R.drawable.filtericons_tiltshift, (int) R.string.filtericon_tiltShift), new FilterInfo(16, (int) R.drawable.filtericons_retrolux, (int) R.string.retrolux), new FilterInfo(17, (int) R.drawable.filtericons_frames, (int) R.string.frames)};

    private static class FilterInfo {
        public static final int NO_TITLE_ID = -1;
        private final int coverResId;
        private final int filterType;
        private final String title;
        private final int titleResId;

        public FilterInfo(int type, int coverResId, int titleResId) {
            this.filterType = type;
            this.coverResId = coverResId;
            this.titleResId = titleResId;
            this.title = null;
        }

        public FilterInfo(int type, int coverResId, String title) {
            this.filterType = type;
            this.coverResId = coverResId;
            this.titleResId = -1;
            this.title = title;
        }

        public int getFilterType() {
            return this.filterType;
        }

        public int getCoverResId() {
            return this.coverResId;
        }

        public String getTitle(Context context) {
            return this.titleResId == -1 ? this.title : context.getString(this.titleResId);
        }
    }

    private FilterList() {
    }

    public static int getItemCount() {
        return FILTERS.length;
    }

    public static String getTitle(int filterType, Context context) {
        for (FilterInfo info : FILTERS) {
            if (info.getFilterType() == filterType) {
                return info.getTitle(context);
            }
        }
        return null;
    }

    public static View getItemView(Context context, int index) {
        if (index < 0 || index >= FILTERS.length) {
            return null;
        }
        FilterInfo filterInfo = FILTERS[index];
        View view = FilterListItemView.createView(context, filterInfo.getCoverResId(), filterInfo.getTitle(context));
        view.setTag(Integer.valueOf(filterInfo.getFilterType()));
        view.setEnabled(filterInfo.filterType != -1000);
        return view;
    }
}

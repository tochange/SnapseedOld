package com.niksoftware.snapseed.controllers.adapters;

import android.content.Context;
import android.graphics.Bitmap;

import com.niksoftware.snapseed.core.filterparameters.FilmFilterParameter;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;

public class FilmItemListAdapter extends StyleItemListAdapter {
    public FilmItemListAdapter(Context context, FilterParameter filterParameter, int styleParamId, Bitmap stylePreviewSource) {
        super(context, filterParameter, styleParamId, stylePreviewSource);
    }

    protected int getItemCountForPreviewRendering(FilterParameter param, int paramId) {
        return FilmFilterParameter.getStackCount();
    }

    public String getItemText(Context context, Integer itemId) {
        switch (itemId.intValue()) {
            case 0:
                return context.getString(R.string.film_warm);
            case 1:
                return context.getString(R.string.film_cross);
            case 2:
                return context.getString(R.string.film_bw);
            case 3:
                return context.getString(R.string.film_cool);
            case 4:
                return context.getString(R.string.film_contrast);
            case 5:
                return context.getString(R.string.film_vintage);
            default:
                return "";
        }
    }
}

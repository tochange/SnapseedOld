package com.niksoftware.snapseed.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.controllers.FilterController;
import com.niksoftware.snapseed.core.DeviceDefs;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.core.filterparameters.UPointFilterParameter;

/* compiled from: ScaleParameterDisplay */
class ScaleParameterDisplayView extends View {
    static boolean s_canCutString = false;
    static String s_lang = null;
    private SparseArray<BitmapDrawable> _backgrounds = new SparseArray();
    private int _height;
    private int _horzTextPadding;
    private int _titleFontSize;
    private int _titleTopOffset;
    private int _valueFontSize;
    private int _width;

    public ScaleParameterDisplayView() {
        super(MainActivity.getMainActivity());
        Resources resources = getResources();
        this._titleFontSize = resources.getDimensionPixelSize(R.dimen.tb_scale_title_font_size);
        this._valueFontSize = resources.getDimensionPixelSize(R.dimen.tb_scale_value_font_size);
        this._titleTopOffset = resources.getDimensionPixelSize(R.dimen.tb_scale_title_top_offset);
        this._horzTextPadding = resources.getDimensionPixelSize(R.dimen.tb_scale_horz_text_padding);
    }

    void fill_backgrounds() {
        FilterController controller = MainActivity.getMainActivity().getFilterController();
        for (int p : controller.getGlobalAdjustmentParameters()) {
            get_background(controller.getScaleBackgroundImageId(p));
        }
    }

    private FilterParameter getFilterParameter() {
        FilterParameter filterParameter = MainActivity.getFilterParameter();
        return (filterParameter == null || !(filterParameter instanceof UPointFilterParameter)) ? filterParameter : ((UPointFilterParameter) filterParameter).getActiveUPoint();
    }

    private BitmapDrawable get_background(int resource_id) {
        BitmapDrawable backgroundDrawable = (BitmapDrawable) this._backgrounds.get(resource_id);
        if (backgroundDrawable != null) {
            return backgroundDrawable;
        }
        backgroundDrawable = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), resource_id));
        backgroundDrawable.setBounds(0, 0, backgroundDrawable.getIntrinsicWidth(), backgroundDrawable.getIntrinsicHeight());
        this._backgrounds.put(resource_id, backgroundDrawable);
        return backgroundDrawable;
    }

    private String fitStringToDisplay(String value, boolean isPortrait) {
        Context context = getContext();
        String title = value;
        if (value.compareToIgnoreCase(context.getString(R.string.select_a_control_point)) == 0 || value.compareToIgnoreCase(context.getString(R.string.add_a_control_point)) == 0) {
            return value;
        }
        Paint paint = new Paint();
        paint.setColor(-5197657);
        paint.setTextAlign(Align.CENTER);
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize((float) this._titleFontSize);
        float width = paint.measureText(value, 0, value.length());
        Paint paintValue = new Paint();
        paintValue.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/DS-DIGIB.ttf"));
        paintValue.setColor(-5197657);
        paintValue.setTextAlign(Align.CENTER);
        paintValue.setTextSize((float) this._valueFontSize);
        paintValue.setAntiAlias(true);
        paintValue.setSubpixelText(true);
        if (width > (((float) getMeasuredWidth()) - (isPortrait ? 0.0f : paintValue.measureText("0000", 0, 4))) - ((float) (this._horzTextPadding * 2))) {
            if (s_lang == null) {
                boolean z;
                s_lang = context.getResources().getConfiguration().locale.getLanguage();
                if (!(s_lang.equals("de") || s_lang.equals("en") || s_lang.equals("ja") || s_lang.equals("fr") || s_lang.equals("es") || s_lang.equals("it") || s_lang.equals("pt") || s_lang.equals("ar") || s_lang.equals("zh-rCN") || s_lang.equals("zh-rHK"))) {
                    s_lang = "en";
                }
                if (s_lang.equals("en") || s_lang.equals("es") || s_lang.equals("it") || s_lang.equals("fr") || s_lang.equals("pt")) {
                    z = true;
                } else {
                    z = false;
                }
                s_canCutString = z;
            }
            if (s_canCutString) {
                String[] array = title.split(" ");
                if (array.length > 1) {
                    title = s_lang.equals("en") ? array[0] : array[array.length - 1];
                }
            }
        }
        return title;
    }

    protected void onDraw(Canvas canvas) {
        BitmapDrawable backgroundImage;
        FilterController controller = MainActivity.getMainActivity().getFilterController();
        FilterParameter filter = getFilterParameter();
        Context context = getContext();
        int activeParameter = filter != null ? filter.getActiveFilterParameter() : 1000;
        if (!DeviceDefs.isTablet() || filter == null) {
            backgroundImage = get_background(R.drawable.gfx_ct_display_empty);
        } else {
            backgroundImage = get_background(controller.getScaleBackgroundImageId(activeParameter));
        }
        backgroundImage.draw(canvas);
        String title = controller.getParameterTitle(activeParameter);
        Paint paint = new Paint();
        paint.setColor(-5197657);
        paint.setTextAlign(Align.CENTER);
        float textSize = (float) this._titleFontSize;
        if (DeviceDefs.isTablet()) {
            int filterType = controller.getFilterType();
            if (filterType == 3) {
                if (title.compareToIgnoreCase(context.getString(R.string.select_a_control_point)) == 0) {
                    textSize *= 0.88f;
                } else {
                    if (title.compareToIgnoreCase(context.getString(R.string.add_a_control_point)) == 0) {
                        textSize *= 0.933f;
                    }
                }
            } else if (filterType == 17) {
                textSize *= 0.88f;
            }
        }
        paint.setAntiAlias(true);
        paint.setSubpixelText(true);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextSize(textSize);
        Rect textDims = new Rect();
        paint.getTextBounds("AJ", 0, 1, textDims);
        if (DeviceDefs.isTablet()) {
            canvas.drawText(title, (float) (this._width / 2), (float) this._titleTopOffset, paint);
        } else if (activeParameter != 1000) {
            String valueString;
            Paint paintValue = new Paint();
            paintValue.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/DS-DIGIB.ttf"));
            paintValue.setColor(-5197657);
            paintValue.setTextAlign(Align.CENTER);
            paintValue.setTextSize((float) this._valueFontSize);
            paintValue.setAntiAlias(true);
            paintValue.setSubpixelText(true);
            int textHeightInPixel = textDims.bottom - textDims.top;
            int value = filter.getParameterValueOld(activeParameter);
            switch (filter.getFilterType()) {
                case 5:
                    valueString = String.format("%+1.2f°", new Object[]{Float.valueOf(((float) value) / 100.0f)});
                    break;
                case 6:
                case 20:
                    valueString = filter.getParameterDescription(getContext(), 42, Integer.valueOf(value));
                    break;
                default:
                    if (value <= 0) {
                        valueString = Integer.toString(value);
                        break;
                    } else {
                        valueString = "+" + Integer.toString(value);
                        break;
                    }
            }
            int orientation = ((WindowManager) context.getSystemService("window")).getDefaultDisplay().getRotation();
            boolean isPortrait = orientation == 0 || orientation == 2;
            title = fitStringToDisplay(title, isPortrait);
            if (isPortrait) {
                canvas.drawText(title, (float) (this._width / 2), (float) (((this._height - (textHeightInPixel * 2)) / 3) + textHeightInPixel), paint);
                canvas.drawText(valueString, (float) (this._width / 2), (float) (this._height - ((this._height - (textHeightInPixel * 2)) / 3)), paintValue);
                return;
            }
            paint.getTextBounds(title, 0, title.length(), textDims);
            canvas.drawText(title, (float) (((textDims.right - textDims.left) / 2) + this._horzTextPadding), (float) ((this._height / 2) + (textHeightInPixel / 2)), paint);
            paint.getTextBounds(valueString, 0, valueString.length(), textDims);
            canvas.drawText(valueString, (float) ((this._width - ((textDims.right - textDims.left) / 2)) - this._horzTextPadding), (float) ((this._height / 2) + (textHeightInPixel / 2)), paintValue);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        BitmapDrawable bitmap = get_background(R.drawable.gfx_ct_display_empty);
        this._width = bitmap.getIntrinsicWidth();
        this._height = bitmap.getIntrinsicHeight();
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(this._width, 1073741824), MeasureSpec.makeMeasureSpec(this._height, 1073741824));
    }
}

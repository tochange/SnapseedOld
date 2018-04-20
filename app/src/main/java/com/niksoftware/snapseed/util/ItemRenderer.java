package com.niksoftware.snapseed.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout.Alignment;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.core.DeviceDefs;
import com.niksoftware.snapseed.views.HelpOverlayView;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ItemRenderer {
    private static final boolean RENDER_DEBUG_FRAME = false;
    private static final Paint debugPaint = new Paint();
    private static final Map<String, Integer> fontSizes;
    private static final Typeface typeface = Typeface.createFromAsset(MainActivity.getMainActivity().getAssets(), "fonts/Plakkaat.ttf");

    public static class Size {
        public final int height;
        public final int width;

        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }

    static {
        Map<String, Integer> fontSizesTmp = new HashMap(6);
        fontSizesTmp.put("default", Integer.valueOf(12));
        fontSizesTmp.put("default_tablet", Integer.valueOf(18));
        fontSizesTmp.put("tall", Integer.valueOf(14));
        fontSizesTmp.put("tall_tablet", Integer.valueOf(21));
        fontSizesTmp.put("extreme", Integer.valueOf(40));
        fontSizesTmp.put("extreme_tablet", Integer.valueOf(56));
        fontSizes = Collections.unmodifiableMap(fontSizesTmp);
        debugPaint.setARGB(255, 255, 0, 0);
        debugPaint.setStyle(Style.STROKE);
    }

    private ItemRenderer() {
    }

    public static void drawImageItem(Canvas canvas, Rect itemRect, AttributeMap attributeMap) {
        Bitmap bitmap = BitmapFactory.decodeResource(MainActivity.getMainActivity().getResources(), ContextHelper.getDrawableResourceId(attributeMap.getAttributeValue("img")));
        int degrees = 0;
        String type = attributeMap.getAttributeValue("type");
        if (type != null && type.equalsIgnoreCase("open_save_share")) {
            Point rotationCenter = ItemBoundsCalculator.pointOfInterestInRect(itemRect, attributeMap.getAttributeValue("align"));
            canvas.save();
            degrees = getIntValue(attributeMap.getAttributeValue("rotation"), 0);
            canvas.translate((float) rotationCenter.x, (float) rotationCenter.y);
            canvas.rotate((float) degrees);
            canvas.translate((float) (-rotationCenter.x), (float) (-rotationCenter.y));
        }
        canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), itemRect, new Paint());
        if (degrees != 0) {
            canvas.restore();
        }
    }

    public static void drawTextItem(Canvas canvas, Rect itemRect, AttributeMap attributeMap) {
        int x = itemRect.left;
        int y = itemRect.top;
        int degrees = 0;
        Point rotationCenter = ItemBoundsCalculator.pointOfInterestInRect(itemRect, attributeMap.getAttributeValue("align"));
        if (attributeMap.hasAttribute("rotation")) {
            canvas.save();
            degrees = getIntValue(attributeMap.getAttributeValue("rotation"), 0);
            canvas.translate((float) rotationCenter.x, (float) rotationCenter.y);
            canvas.rotate((float) degrees);
            canvas.translate((float) (-rotationCenter.x), (float) (-rotationCenter.y));
        }
        String type = attributeMap.getAttributeValue("type");
        if (type != null && type.equalsIgnoreCase("dismiss")) {
            drawDismissChrome(canvas, itemRect);
        }
        canvas.translate((float) x, (float) y);
        getStaticLayout(attributeMap).draw(canvas);
        canvas.translate((float) (-x), (float) (-y));
        if (degrees != 0) {
            canvas.restore();
        }
    }

    public static Size itemSize(String nodeName, AttributeMap as) {
        if (nodeName.equalsIgnoreCase(HelpOverlayView.IMAGE_NODE)) {
            return itemSizeOfImage(as);
        }
        if (nodeName.equalsIgnoreCase(HelpOverlayView.TEXT_NODE)) {
            return itemSizeOfText(as);
        }
        return new Size(0, 0);
    }

    private static StaticLayout getStaticLayout(AttributeMap attributeMap) {
        String textToDraw = getTextForItem(attributeMap.getAttributeValue("txt"));
        TextPaint textPaint = getTextPaint(attributeMap);
        String fixWidth = "";
        if (attributeMap.hasAttribute("fixWidth")) {
            fixWidth = attributeMap.getAttributeValue("fixWidth");
            if (fixWidth.endsWith("%")) {
                textToDraw = textToDraw.replaceAll("\\n", " ");
            }
        }
        return new StaticLayout(textToDraw, textPaint, getDesiredWidth(textToDraw, textPaint, fixWidth), Alignment.ALIGN_CENTER, 1.0f, 10.0f, false);
    }

    private static int getDesiredWidth(String txt, TextPaint textPaint, String fixWidth) {
        if (fixWidth.endsWith("%")) {
            return (int) Math.floor(((double) MainActivity.getWorkingAreaView().getWidth()) * (((double) Integer.parseInt(fixWidth.substring(0, fixWidth.length() - 1))) / 100.0d));
        } else if (fixWidth.equals("")) {
            return ((int) StaticLayout.getDesiredWidth(txt, textPaint)) + 1;
        } else {
            return (int) (((float) getIntValue(fixWidth, 350)) * DeviceDefs.getScreenDensityRatio());
        }
    }

    private static TextPaint getTextPaint(AttributeMap attributeMap) {
        TextPaint textPaint = new TextPaint(new Paint());
        textPaint.setTypeface(typeface);
        textPaint.setTextSize((float) decodeFontSize(attributeMap.getAttributeValue("fontsize")));
        textPaint.setColor(-1);
        textPaint.setAntiAlias(true);
        return textPaint;
    }

    private static String getTextForItem(String value) {
        int resId = ContextHelper.getStringResourceId(value);
        if (!value.equalsIgnoreCase("overlay_open_save_share")) {
            return getTextForItemWithDefault(resId, value);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getTextForItemWithDefault(ContextHelper.getStringResourceId("save_btn"), "save_btn"));
        int resourceId = ContextHelper.getStringResourceId("share_btn");
        stringBuilder.append(String.format(", %s", new Object[]{getTextForItemWithDefault(resourceId, "share_btn")}));
        resourceId = ContextHelper.getStringResourceId("open_library_btn");
        stringBuilder.append(String.format(",%n%s...", new Object[]{getTextForItemWithDefault(resourceId, "open_library_btn")}));
        return stringBuilder.toString();
    }

    private static String getTextForItemWithDefault(int resId, String dft) {
        if (resId == -1) {
            return dft;
        }
        return MainActivity.getMainActivity().getString(resId).replaceAll("(%)(\\d+)(\\$)(n)", "\n");
    }

    private static Size itemSizeOfText(AttributeMap as) {
        StaticLayout staticLayout = getStaticLayout(as);
        return new Size(staticLayout.getWidth(), staticLayout.getHeight());
    }

    private static Size itemSizeOfImage(AttributeMap as) {
        Bitmap bitmap = BitmapFactory.decodeResource(MainActivity.getMainActivity().getResources(), ContextHelper.getDrawableResourceId(as.getAttributeValue("img")));
        return new Size(bitmap.getWidth(), bitmap.getHeight());
    }

    private static int decodeFontSize(String fontsize) {
        if (fontsize == null) {
            fontsize = "default";
        } else {
            fontsize = fontsize.toLowerCase();
            if (!fontSizes.containsKey(fontsize)) {
                fontsize = "default";
            }
        }
        if (DeviceDefs.isTablet()) {
            fontsize = fontsize + "_tablet";
        }
        return (int) (((float) ((Integer) fontSizes.get(fontsize)).intValue()) * DeviceDefs.getScreenDensityRatio());
    }

    private static void drawDismissChrome(Canvas canvas, Rect itemRect) {
        Bitmap frame = BitmapFactory.decodeResource(MainActivity.getMainActivity().getResources(), R.drawable.dismiss_bg);
        Rect src = new Rect(0, 0, frame.getWidth(), frame.getHeight());
        int x1 = itemRect.left - 10;
        int y1 = itemRect.top - 15;
        Rect dst = new Rect(x1, y1, (itemRect.width() + x1) + 20, (itemRect.height() + y1) + 20);
        Paint antiAliasPaint = new Paint();
        antiAliasPaint.setSubpixelText(true);
        antiAliasPaint.setAntiAlias(true);
        canvas.drawBitmap(frame, src, dst, antiAliasPaint);
    }

    private static int getIntValue(String value, int defaultValue) {
        if (value != null) {
            try {
                defaultValue = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                Log.i("Snapseed", String.format("NumberFormatException: %s", new Object[]{value}));
            }
        }
        return defaultValue;
    }
}

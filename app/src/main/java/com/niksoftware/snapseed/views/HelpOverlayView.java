package com.niksoftware.snapseed.views;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.content.res.XmlResourceParser;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.Log;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.core.DeviceDefs;
import com.niksoftware.snapseed.util.AttributeMap;
import com.niksoftware.snapseed.util.ItemBoundsCalculator;
import com.niksoftware.snapseed.util.ItemRenderer;
import com.niksoftware.snapseed.util.XmlHelper;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HelpOverlayView extends ViewGroup {
    private static final float FADE_ANIMATION_ACCELERATION = 1.5f;
    private static final String HELPOVERLAY = "helpoverlay";
    public static final String IMAGE_NODE = "image";
    private static final String ITEMS_NODE = "items";
    private static final int OVERLAY_RENDER_FLAGS = 387;
    private static final int QUARTER_SECOND = 250;
    public static final String TEXT_NODE = "text";
    private final ItemBoundsCalculator anchors = new ItemBoundsCalculator();
    private Bitmap bitmap;
    private final ImageView imageView;

    private class RenderOverlayBitmap extends AsyncTask<RenderOverlayBitmapParams, Integer, Bitmap> {
        private RenderOverlayBitmap() {
        }

        protected Bitmap doInBackground(RenderOverlayBitmapParams... params) {
            if (params == null || params.length == 0) {
                return null;
            }
            RenderOverlayBitmapParams param = params[0];
            Bitmap bitmap = Bitmap.createBitmap(param.width, param.height, Config.ARGB_8888);
            bitmap.eraseColor(0);
            HelpOverlayView.this.renderOverlayBitmap(param.resId, param.width, param.height, bitmap);
            return bitmap;
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Bitmap bitmap) {
            HelpOverlayView.this.updateViewWithOverlay(bitmap);
        }
    }

    private class RenderOverlayBitmapParams {
        private final int height;
        private final int resId;
        private final int width;

        RenderOverlayBitmapParams(int resId, int width, int height) {
            this.resId = resId;
            this.width = width;
            this.height = height;
        }
    }

    public HelpOverlayView(Context context) {
        super(context);
        this.imageView = new ImageView(context);
        this.imageView.setAlpha(0.0f);
        addView(this.imageView, new LayoutParams(-1, -1));
        setBackgroundColor(getResources().getColor(R.color.helpoverlay_background));
        setWillNotDraw(true);
    }

    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (this.imageView != null) {
            this.imageView.layout(0, 0, right - left, bottom - top);
        }
    }

    public void setUp(int resId, int width, int height) {
        Bitmap oldBitmap = this.bitmap;
        this.bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        this.bitmap.eraseColor(0);
        if (!(oldBitmap == null || oldBitmap.isRecycled())) {
            oldBitmap.recycle();
        }
        new RenderOverlayBitmap().execute(new RenderOverlayBitmapParams[]{new RenderOverlayBitmapParams(resId, width, height)});
    }

    private void renderOverlayBitmap(int resId, int rootViewWidth, int rootViewHeight, Bitmap bitmap) {
        boolean z = false;
        if (bitmap != null) {
            Canvas canvas = new Canvas(bitmap);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, OVERLAY_RENDER_FLAGS));
            if (rootViewWidth > rootViewHeight) {
                z = true;
            }
            parseXMLResource(resId, canvas, z);
        }
    }

    private void updateViewWithOverlay(Bitmap bitmap) {
        if (bitmap != null) {
            this.bitmap = bitmap;
            this.imageView.setImageBitmap(bitmap);
            ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(this.imageView, "alpha", new float[]{0.0f, 1.0f});
            objectAnimator.setDuration(250);
            objectAnimator.setInterpolator(new DecelerateInterpolator(FADE_ANIMATION_ACCELERATION));
            objectAnimator.start();
        }
    }

    private void parseXMLResource(int resId, Canvas canvas, boolean isLandscape) {
        try {
            XmlResourceParser parser = MainActivity.getMainActivity().getResources().getXml(resId);
            while (true) {
                try {
                    if (parser.next() == 2) {
                        break;
                    }
                } catch (IOException e) {
                    Log.i("Snapseed", e.toString());
                } catch (XmlPullParserException e2) {
                    Log.i("Snapseed", e2.toString());
                } finally {
                    parser.close();
                }
            }
            processHelperOverlayTag(parser, parser.getName(), canvas, isLandscape);
        } catch (NotFoundException e3) {
            Log.i("Snapseed", e3.toString());
        }
    }

    private void processHelperOverlayTag(XmlPullParser parser, String tag, Canvas canvas, boolean isLandscape) throws IOException, XmlPullParserException {
        if (tag.equalsIgnoreCase(HELPOVERLAY)) {
            int nextEvent = parser.getEventType() == 3 ? parser.getEventType() : parser.next();
            while (true) {
                if (nextEvent == 3 && tag.equals(parser.getName())) {
                    parser.require(3, null, tag);
                    return;
                }
                if (nextEvent == 2) {
                    processItems(parser, parser.getName(), canvas, isLandscape);
                }
                nextEvent = parser.next();
            }
        }
    }

    private void processItems(XmlPullParser parser, String tag, Canvas canvas, boolean isLandscape) throws IOException, XmlPullParserException {
        if (tag.equalsIgnoreCase(ITEMS_NODE)) {
            boolean isTablet = DeviceDefs.isTablet();
            String orientation = XmlHelper.findAttributeValueForKey("orientation", parser);
            if (!orientation.endsWith("_phone") || !isTablet) {
                if ((orientation.endsWith("_phone") || isTablet) && orientation.startsWith("landscape") == isLandscape) {
                    int nextEvent = parser.getEventType() == 3 ? parser.getEventType() : parser.next();
                    while (true) {
                        if (nextEvent == 3 && tag.equals(parser.getName())) {
                            parser.require(3, null, tag);
                            return;
                        }
                        if (nextEvent == 2) {
                            processSingleItem(parser, parser.getName(), canvas);
                        }
                        nextEvent = parser.next();
                    }
                }
            }
        }
    }

    private void processSingleItem(XmlPullParser parser, String tag, Canvas canvas) {
        if (tag.equalsIgnoreCase(IMAGE_NODE) || tag.equalsIgnoreCase(TEXT_NODE)) {
            addItem(tag, parser, canvas);
        }
    }

    private void addItem(String nodeName, XmlPullParser parser, Canvas canvas) {
        AttributeMap attributeMap = AttributeMap.fromXml(parser);
        Rect itemRect = this.anchors.getItemRect(nodeName, attributeMap);
        if (nodeName.equalsIgnoreCase(IMAGE_NODE)) {
            ItemRenderer.drawImageItem(canvas, itemRect, attributeMap);
        } else if (nodeName.equalsIgnoreCase(TEXT_NODE)) {
            ItemRenderer.drawTextItem(canvas, itemRect, attributeMap);
        }
    }
}

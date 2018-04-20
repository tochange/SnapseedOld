package com.niksoftware.snapseed.util;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.core.DeviceDefs;
import com.niksoftware.snapseed.util.ItemRenderer.Size;
import com.niksoftware.snapseed.views.EditingToolBar;
import com.niksoftware.snapseed.views.GlobalToolBar;
import com.niksoftware.snapseed.views.RootView;
import java.util.HashMap;
import java.util.Map;

public class ItemBoundsCalculator {
    private static final String ANCHOR_APPLYBUTTON = "APPLYBUTTON";
    private static final String ANCHOR_BACKBUTTON = "BACKBUTTON";
    private static final String ANCHOR_CAMERABUTTON = "CAMERABUTTON";
    private static final String ANCHOR_FILTERBUTTON_LEFT = "FILTERBUTTONLEFT";
    private static final String ANCHOR_FILTERBUTTON_RIGHT = "FILTERBUTTONRIGHT";
    private static final String ANCHOR_FILTERLIST = "FILTERLIST";
    private static final String ANCHOR_LIBRARYBUTTON = "LIBRARYBUTTON";
    private static final String ANCHOR_MENUBUTTON = "MENUBUTTON";
    private static final String ANCHOR_REVERTBUTTON = "REVERTBUTTON";
    private static final String ANCHOR_SAVEBUTTON = "SAVEBUTTON";
    private static final String ANCHOR_SCREEN = "SCREEN";
    private static final String ANCHOR_SHAREBUTTON = "SHAREBUTTON";
    private static final Rect EMPTY_RECT = new Rect();
    private final Map<String, Rect> anchorMap = new HashMap();

    public Rect getItemRect(String nodeName, AttributeMap attributeMap) {
        Point anchorPoint = findAnchorPoint(findAnchorRect(attributeMap.getAttributeValue("anchor")), attributeMap.getAttributeValue("anchoralign"));
        Size itemSize = ItemRenderer.itemSize(nodeName, attributeMap);
        Point itemPoint = addSpacing(attributeMap, findItemBasePoint(anchorPoint, attributeMap.getAttributeValue("align"), itemSize));
        Rect itemRect = trimRect(new Rect(itemPoint.x, itemPoint.y, itemPoint.x + itemSize.width, itemPoint.y + itemSize.height));
        String namedItem = attributeMap.getAttributeValue("name");
        if (namedItem != null) {
            this.anchorMap.put(namedItem, itemRect);
        }
        return itemRect;
    }

    private static Rect findGlobalAnchor(String anchorName) {
        if (anchorName.equalsIgnoreCase(ANCHOR_APPLYBUTTON)) {
            return findApplyButton();
        }
        if (anchorName.equalsIgnoreCase(ANCHOR_BACKBUTTON)) {
            return findBackButton();
        }
        if (anchorName.equalsIgnoreCase(ANCHOR_FILTERBUTTON_LEFT)) {
            return findFilterButtonLeft();
        }
        if (anchorName.equalsIgnoreCase(ANCHOR_FILTERBUTTON_RIGHT)) {
            return findFilterButtonRight();
        }
        if (anchorName.equalsIgnoreCase(ANCHOR_FILTERLIST)) {
            return findFilterList();
        }
        if (anchorName.equalsIgnoreCase(ANCHOR_LIBRARYBUTTON)) {
            return findLibraryList();
        }
        if (anchorName.equalsIgnoreCase(ANCHOR_CAMERABUTTON)) {
            return findCameraButton();
        }
        if (anchorName.equalsIgnoreCase(ANCHOR_MENUBUTTON)) {
            return findMenuButton();
        }
        if (anchorName.equalsIgnoreCase(ANCHOR_SHAREBUTTON)) {
            return findShareButton();
        }
        if (anchorName.equalsIgnoreCase(ANCHOR_SAVEBUTTON)) {
            return findSaveButton();
        }
        if (anchorName.equalsIgnoreCase(ANCHOR_REVERTBUTTON)) {
            return findRevertButton();
        }
        if (!anchorName.equalsIgnoreCase(ANCHOR_SCREEN)) {
            return EMPTY_RECT;
        }
        MainActivity.getMainActivity();
        RootView rootView = MainActivity.getRootView();
        return new Rect(0, 0, rootView.getWidth(), rootView.getHeight());
    }

    private static Rect findFilterList() {
        MainActivity.getMainActivity();
        RootView rootView = MainActivity.getRootView();
        return rootView == null ? EMPTY_RECT : calcAnchorBounds(rootView.getFilterList());
    }

    private static Rect findLibraryList() {
        return EMPTY_RECT;
    }

    private static Rect findCameraButton() {
        return EMPTY_RECT;
    }

    private static Rect findMenuButton() {
        MainActivity.getMainActivity();
        RootView rootView = MainActivity.getRootView();
        return rootView == null ? EMPTY_RECT : new Rect(rootView.getWidth() - 50, 0, rootView.getWidth(), MainActivity.getMainActivity().getActionBar().getHeight());
    }

    private static Rect findShareButton() {
        MainActivity.getMainActivity();
        RootView rootView = MainActivity.getRootView();
        GlobalToolBar globalToolBar = rootView == null ? null : rootView.getGlobalToolBar();
        return globalToolBar == null ? EMPTY_RECT : calcAnchorBounds(globalToolBar.getShareButton());
    }

    private static Rect findSaveButton() {
        MainActivity.getMainActivity();
        RootView rootView = MainActivity.getRootView();
        GlobalToolBar globalToolBar = rootView == null ? null : rootView.getGlobalToolBar();
        return globalToolBar == null ? EMPTY_RECT : calcAnchorBounds(globalToolBar.getSaveButton());
    }

    private static Rect findRevertButton() {
        MainActivity.getMainActivity();
        RootView rootView = MainActivity.getRootView();
        GlobalToolBar globalToolBar = rootView == null ? null : rootView.getGlobalToolBar();
        return globalToolBar == null ? EMPTY_RECT : calcAnchorBounds(globalToolBar.getRevertButton());
    }

    private static Rect findApplyButton() {
        MainActivity.getMainActivity();
        EditingToolBar editingToolBar = MainActivity.getEditingToolbar();
        return editingToolBar == null ? EMPTY_RECT : calcAnchorBounds(editingToolBar.getApplyButton());
    }

    private static Rect findFilterButtonLeft() {
        MainActivity.getMainActivity();
        EditingToolBar editingToolBar = MainActivity.getEditingToolbar();
        return editingToolBar == null ? EMPTY_RECT : calcAnchorBounds(editingToolBar.getFilterButtonLeft());
    }

    private static Rect findFilterButtonRight() {
        MainActivity.getMainActivity();
        EditingToolBar editingToolBar = MainActivity.getEditingToolbar();
        return editingToolBar == null ? EMPTY_RECT : calcAnchorBounds(editingToolBar.getFilterButtonRight());
    }

    private static Rect findBackButton() {
        MainActivity.getMainActivity();
        EditingToolBar editingToolBar = MainActivity.getEditingToolbar();
        return editingToolBar == null ? EMPTY_RECT : calcAnchorBounds(editingToolBar.getBackButton());
    }

    private static Point findAnchorPoint(Rect r, String anchor) {
        if (anchor == null) {
            return new Point(r.left, r.top);
        }
        if (!anchor.endsWith("°")) {
            return pointOfInterestInRect(r, anchor);
        }
        int angle = 360 - Integer.parseInt(anchor.substring(0, anchor.length() - 1));
        MainActivity.getMainActivity();
        RootView rootView = MainActivity.getRootView();
        float rayLength = (float) (rootView.getWidth() + rootView.getHeight());
        Point midPoint = new Point(r.centerX(), r.centerY());
        Point rotPoint = new Point(((int) (((double) rayLength) * Math.cos(Math.toRadians((double) angle)))) + midPoint.x, ((int) (((double) rayLength) * Math.sin(Math.toRadians((double) angle)))) + midPoint.y);
        Point intersection1;
        if (isInInterval(270, 360, angle)) {
            intersection1 = getIntersectionPoint(new Point(r.right, r.top), new Point(r.right, r.bottom), midPoint, rotPoint);
            if (isInInterval(r.top, r.bottom, intersection1.y)) {
                return intersection1;
            }
            return getIntersectionPoint(new Point(r.left, r.top), new Point(r.right, r.top), midPoint, rotPoint);
        } else if (isInInterval(180, 270, angle)) {
            intersection1 = getIntersectionPoint(new Point(r.left, r.top), new Point(r.right, r.top), midPoint, rotPoint);
            if (isInInterval(r.left, r.right, intersection1.x)) {
                return intersection1;
            }
            return getIntersectionPoint(new Point(r.left, r.top), new Point(r.left, r.bottom), midPoint, rotPoint);
        } else if (isInInterval(90, 180, angle)) {
            intersection1 = getIntersectionPoint(new Point(r.left, r.top), new Point(r.left, r.bottom), midPoint, rotPoint);
            if (isInInterval(r.top, r.bottom, intersection1.y)) {
                return intersection1;
            }
            return getIntersectionPoint(new Point(r.left, r.bottom), new Point(r.right, r.bottom), midPoint, rotPoint);
        } else {
            intersection1 = getIntersectionPoint(new Point(r.left, r.bottom), new Point(r.right, r.bottom), midPoint, rotPoint);
            return !isInInterval(r.left, r.right, intersection1.x) ? getIntersectionPoint(new Point(r.right, r.top), new Point(r.right, r.bottom), midPoint, rotPoint) : intersection1;
        }
    }

    public static Point pointOfInterestInRect(Rect rect, String anchor) {
        if (anchor == null) {
            return new Point(rect.centerX(), rect.centerY());
        }
        if (anchor.equalsIgnoreCase("CENTER_CENTER")) {
            return new Point(rect.centerX(), rect.centerY());
        }
        if (anchor.equalsIgnoreCase("CENTER_LEFT")) {
            return new Point(rect.left, rect.centerY());
        }
        if (anchor.equalsIgnoreCase("CENTER_RIGHT")) {
            return new Point(rect.right, rect.centerY());
        }
        if (anchor.equalsIgnoreCase("TOP_CENTER")) {
            return new Point(rect.centerX(), rect.top);
        }
        if (anchor.equalsIgnoreCase("TOP_LEFT")) {
            return new Point(rect.left, rect.top);
        }
        if (anchor.equalsIgnoreCase("TOP_RIGHT")) {
            return new Point(rect.right, rect.top);
        }
        if (anchor.equalsIgnoreCase("BOTTOM_CENTER")) {
            return new Point(rect.centerX(), rect.bottom);
        }
        if (anchor.equalsIgnoreCase("BOTTOM_LEFT")) {
            return new Point(rect.left, rect.bottom);
        }
        if (anchor.equalsIgnoreCase("BOTTOM_RIGHT")) {
            return new Point(rect.right, rect.bottom);
        }
        return new Point(rect.centerX(), rect.centerY());
    }

    private static Point findItemBasePoint(Point point, String anchor, Size size) {
        if (anchor == null) {
            return new Point(point.x, point.y);
        }
        if (anchor.equalsIgnoreCase("CENTER_CENTER")) {
            return new Point(point.x - (size.width / 2), point.y - (size.height / 2));
        }
        if (anchor.equalsIgnoreCase("CENTER_LEFT")) {
            return new Point(point.x, point.y - (size.height / 2));
        }
        if (anchor.equalsIgnoreCase("CENTER_RIGHT")) {
            return new Point(point.x - size.width, point.y - (size.height / 2));
        }
        if (anchor.equalsIgnoreCase("TOP_CENTER")) {
            return new Point(point.x - (size.width / 2), point.y);
        }
        if (anchor.equalsIgnoreCase("TOP_LEFT")) {
            return new Point(point.x, point.y);
        }
        if (anchor.equalsIgnoreCase("TOP_RIGHT")) {
            return new Point(point.x - size.width, point.y);
        }
        if (anchor.equalsIgnoreCase("BOTTOM_CENTER")) {
            return new Point(point.x - (size.width / 2), point.y - size.height);
        }
        if (anchor.equalsIgnoreCase("BOTTOM_LEFT")) {
            return new Point(point.x, point.y - size.height);
        }
        if (anchor.equalsIgnoreCase("BOTTOM_RIGHT")) {
            return new Point(point.x - size.width, point.y - size.height);
        }
        return new Point(point.x, point.y);
    }

    private Rect findAnchorRect(String anchor) {
        if (anchor == null) {
            return EMPTY_RECT;
        }
        return this.anchorMap.containsKey(anchor) ? (Rect) this.anchorMap.get(anchor) : findGlobalAnchor(anchor);
    }

    private static boolean isInInterval(int low, int high, int value) {
        return low <= value && value <= high;
    }

    private static Point getIntersectionPoint(Point point1, Point point2, Point point3, Point point4) {
        float d = (float) (((point1.x - point2.x) * (point3.y - point4.y)) - ((point1.y - point2.y) * (point3.x - point4.x)));
        float pre = (float) ((point1.x * point2.y) - (point1.y * point2.x));
        float post = (float) ((point3.x * point4.y) - (point3.y * point4.x));
        return new Point((int) (((((float) (point3.x - point4.x)) * pre) - (((float) (point1.x - point2.x)) * post)) / d), (int) (((((float) (point3.y - point4.y)) * pre) - (((float) (point1.y - point2.y)) * post)) / d));
    }

    private static Rect calcAnchorBounds(View view) {
        if (view == null) {
            return EMPTY_RECT;
        }
        Rect rect = new Rect();
        view.getGlobalVisibleRect(rect);
        return rect;
    }

    private static Rect trimRect(Rect toTrim) {
        MainActivity.getMainActivity();
        View rootView = MainActivity.getRootView();
        int width = rootView.getWidth();
        int height = rootView.getHeight();
        Rect result = new Rect(toTrim.left, toTrim.top, toTrim.right, toTrim.bottom);
        if (result.left < 0) {
            result.offset(-result.left, 0);
        }
        if (result.top < 0) {
            result.offset(0, -result.top);
        }
        if (result.right > width) {
            result.offset(width - result.right, 0);
        }
        if (result.bottom > height) {
            result.offset(0, height - result.bottom);
        }
        return result;
    }

    private static Point addSpacing(AttributeMap attributeMap, Point point) {
        String spacing = attributeMap.getAttributeValue("spacing");
        if (spacing == null) {
            return point;
        }
        String[] values = spacing.split(",");
        if (values.length != 2) {
            return point;
        }
        int offX = Integer.parseInt(values[0].trim());
        int offY = Integer.parseInt(values[1].trim());
        float multiplier = DeviceDefs.getScreenDensityRatio();
        return new Point(point.x + ((int) (((float) offX) * multiplier)), point.y + ((int) (((float) offY) * multiplier)));
    }
}

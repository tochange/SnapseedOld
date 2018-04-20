package com.niksoftware.snapseed.views;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.niksoftware.snapseed.R;
import com.niksoftware.snapseed.views.PopoverWindowItem.HorizontalLine;
import com.niksoftware.snapseed.views.PopoverWindowItem.Text;
import com.niksoftware.snapseed.views.PopoverWindowItem.VerticalLine;
import java.util.ArrayList;
import java.util.Iterator;

public class PopoverWindow extends PopoverWindowView {
    protected static final int ANIM_AUTO = 5;
    protected static final int ANIM_GROW_FROM_CENTER = 3;
    protected static final int ANIM_GROW_FROM_LEFT = 1;
    protected static final int ANIM_GROW_FROM_RIGHT = 2;
    protected static final int ANIM_REFLECT = 4;
    private int _animStyle;
    private int _childPos;
    private Context _context;
    private HorizontalScrollView _hscroller;
    private ArrayList<PopoverWindowItem> _items = new ArrayList();
    private int _layout;
    private OnActionItemClickListener _listener;
    private RelativeLayout _rootView;
    private ScrollView _scroller;
    private LinearLayout _track;

    public interface OnActionItemClickListener {
        void onItemClick(int i);
    }

    private enum ARROW {
        UP,
        DOWN
    }

    public PopoverWindow(Context context, int layout) {
        super(context);
        if (!(layout == 1 || layout == 0)) {
            layout = 1;
        }
        this._layout = layout;
        this._context = context;
        init(context);
        this._animStyle = 5;
        this._childPos = 0;
    }

    private void init(Context context) {
        this._rootView = new RelativeLayout(context);
        this._rootView.setLayoutParams(new LayoutParams(-2, -2));
        this._rootView.setPadding(0, 0, 0, 0);
        this._track = new LinearLayout(context);
        this._track.setOrientation(this._layout);
        this._track.setLayoutParams(new LayoutParams(-2, -2));
        RelativeLayout.LayoutParams l;
        if (this._layout == 1) {
            this._track.setPadding(0, 0, 0, 0);
            this._scroller = new ScrollView(context);
            this._scroller.addView(this._track);
            this._scroller.setId(-1412628479);
            this._scroller.setBackgroundResource(R.drawable.effect_background_2);
            l = new RelativeLayout.LayoutParams(-2, -2);
            l.addRule(3, this._scroller.getId());
            l.setMargins(0, -2, 0, 0);
            this._rootView.addView(this._scroller);
        } else {
            this._track.setPadding(0, 0, 0, 0);
            this._hscroller = new HorizontalScrollView(context);
            this._hscroller.addView(this._track);
            this._hscroller.setId(-1412628479);
            this._hscroller.setBackgroundResource(R.drawable.effect_background_2);
            l = new RelativeLayout.LayoutParams(-2, -2);
            l.addRule(3, this._hscroller.getId());
            l.setMargins(0, -2, 0, 0);
            this._rootView.addView(this._hscroller);
        }
        setContentView(this._rootView);
    }

    public void setAnimStyle(int animStyle) {
        this._animStyle = animStyle;
    }

    public void setOnActionItemClickListener(OnActionItemClickListener onActionItemClickListener) {
        this._listener = onActionItemClickListener;
    }

    public PopoverWindowItem addItem(Drawable icon, String title, boolean enabled) {
        return addItem(icon, title, enabled, this._items.size());
    }

    public PopoverWindowItem addItem(Drawable icon, String title, boolean enabled, final int id) {
        if (this._items.size() > 0) {
            PopoverWindowItem line = this._layout != 1 ? new VerticalLine(this._context, icon, title, true) : new HorizontalLine(this._context, icon, title, true);
            this._items.add(line);
            this._track.addView(line, this._childPos);
            this._childPos++;
        }
        PopoverWindowItem container = new Text(this._context, icon, title, false);
        container.setTag(Integer.valueOf(id));
        container.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                PopoverWindowItem tmpItem = (PopoverWindowItem) v;
                switch (event.getAction()) {
                    case 0:
                        tmpItem.tv.setTextColor(-13882324);
                        break;
                    case 1:
                        if (PopoverWindow.this._listener != null && v.isEnabled()) {
                            PopoverWindow.this._listener.onItemClick(id);
                            break;
                        }
                    case 2:
                        break;
                    default:
                        tmpItem.tv.setTextColor(-1);
                        break;
                }
                return true;
            }
        });
        container.setFocusable(true);
        container.setClickable(true);
        container.setEnabled(enabled);
        this._items.add(container);
        this._track.addView(container, this._childPos);
        this._childPos++;
        layoutItems();
        return container;
    }

    public int getIdForItem(int index) {
        return index > this._items.size() ? -1 : ((Integer) ((PopoverWindowItem) this._items.get(index)).getTag()).intValue();
    }

    private void layoutItems() {
        Iterator i$ = this._items.iterator();
        while (i$.hasNext()) {
            PopoverWindowItem item = (PopoverWindowItem) i$.next();
            if (item != null) {
                if (item.isLine()) {
                    item.setPadding(0, 0, 0, 0);
                } else {
                    int top = 5;
                    int bottom = 5;
                    if (this._layout == 1) {
                        bottom = 10;
                        top = 10;
                    }
                    item.setPadding(5, top, 5, bottom);
                }
            }
        }
    }

    public void show(View anchor) {
        if (anchor != null) {
            preShow();
            int[] location = new int[2];
            anchor.getLocationOnScreen(location);
            show(anchor, new Rect(location[0], location[1], location[0] + anchor.getWidth(), location[1] + anchor.getHeight()));
        }
    }

    public void show(View anchor, int offsetX, int offsetY) {
        if (anchor != null) {
            preShow();
            int[] location = new int[2];
            anchor.getLocationOnScreen(location);
            show(anchor, new Rect(location[0] + offsetX, location[1] + offsetY, location[0] + offsetX, location[1] + offsetY));
        }
    }

    private void show(View anchor, Rect anchorRect) {
        int xPos;
        int yPos;
        this._rootView.setLayoutParams(new LayoutParams(-2, -2));
        this._rootView.measure(-2, -2);
        int rootWidth = this._rootView.getMeasuredWidth();
        int rootHeight = this._rootView.getMeasuredHeight();
        Point p = new Point();
        this._windowManager.getDefaultDisplay().getSize(p);
        if (anchorRect.centerX() - (rootWidth / 2) < p.x) {
            xPos = anchorRect.centerX() - (rootWidth / 2);
        } else if (anchorRect.left + rootWidth > p.x) {
            xPos = anchorRect.left - (rootWidth - anchor.getWidth());
        } else if (anchor.getWidth() > rootWidth) {
            xPos = anchorRect.centerX() - (rootWidth / 2);
        } else {
            xPos = anchorRect.left;
        }
        int dyTop = anchorRect.top;
        int dyBottom = p.y - anchorRect.bottom;
        boolean onTop = dyTop > rootHeight + 30;
        LayoutParams l = this._layout == 1 ? this._scroller.getLayoutParams() : this._hscroller.getLayoutParams();
        if (onTop) {
            if (rootHeight > dyTop) {
                yPos = 15;
                l.height = dyTop - anchor.getHeight();
            } else {
                yPos = anchorRect.top - rootHeight;
            }
            yPos -= 10;
        } else {
            yPos = anchorRect.bottom;
            if (rootHeight > dyBottom) {
                l.height = dyBottom;
            }
            yPos += 10;
        }
        showArrow(onTop ? ARROW.DOWN : ARROW.UP, anchorRect.centerX() - xPos);
        setAnimationStyle(p.x, anchorRect.centerX(), onTop);
        this._wnd.showAtLocation(anchor, 0, xPos, yPos);
    }

    private void setAnimationStyle(int screenWidth, int requestedX, boolean onTop) {
        int i = R.style.Animations.PopUpMenu.Left;
        int i2 = R.style.Animations.PopUpMenu.Center;
        int i3 = R.style.Animations.PopDownMenu.Right;
        int arrowPos = requestedX + 0;
        PopupWindow popupWindow;
        switch (this._animStyle) {
            case 1:
                popupWindow = this._wnd;
                if (!onTop) {
                    i = R.style.Animations.PopDownMenu.Left;
                }
                popupWindow.setAnimationStyle(i);
                return;
            case 2:
                this._wnd.setAnimationStyle(onTop ? R.style.Animations.PopUpMenu.Right : R.style.Animations.PopDownMenu.Right);
                return;
            case 3:
                this._wnd.setAnimationStyle(onTop ? R.style.Animations.PopUpMenu.Center : R.style.Animations.PopDownMenu.Center);
                return;
            case 4:
                this._wnd.setAnimationStyle(onTop ? R.style.Animations.PopUpMenu.Reflect : R.style.Animations.PopDownMenu.Reflect);
                return;
            case 5:
                if (arrowPos <= screenWidth / 4) {
                    popupWindow = this._wnd;
                    if (!onTop) {
                        i = R.style.Animations.PopDownMenu.Left;
                    }
                    popupWindow.setAnimationStyle(i);
                    return;
                } else if (arrowPos <= screenWidth / 4 || arrowPos >= (screenWidth / 4) * 3) {
                    r1 = this._wnd;
                    if (onTop) {
                        i3 = R.style.Animations.PopUpMenu.Right;
                    }
                    r1.setAnimationStyle(i3);
                    return;
                } else {
                    r1 = this._wnd;
                    if (!onTop) {
                        i2 = R.style.Animations.PopDownMenu.Center;
                    }
                    r1.setAnimationStyle(i2);
                    return;
                }
            default:
                return;
        }
    }

    private void showArrow(ARROW hide, int requestedX) {
    }
}

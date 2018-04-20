package com.niksoftware.snapseed.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

public abstract class PopoverWindowItem extends LinearLayout {
    TextView tv;

    public static class HorizontalLine extends PopoverWindowItem {
        ImageView line;

        public HorizontalLine(Context context, Drawable icon, String txt, boolean lineElement) {
            super(context, icon, txt, lineElement);
            setOrientation(0);
            setLayoutParams(new LayoutParams(-1, -2));
            this.line = new ImageView(context);
            this.line.setMinimumHeight(1);
            this.line.setLayoutParams(new LayoutParams(-1, -2));
            this.line.setBackgroundColor(-15000288);
            addView(this.line);
        }

        public void setEnabled(boolean enabled) {
            if (this.line != null) {
                this.line.setEnabled(enabled);
            }
            super.setEnabled(enabled);
        }

        public boolean isLine() {
            return true;
        }
    }

    public static class Text extends PopoverWindowItem {
        ImageView iv;

        public Text(Context context, Drawable icon, String txt, boolean lineElement) {
            super(context, icon, txt, lineElement);
            setOrientation(1);
            setLayoutParams(new LayoutParams(-2, -1));
            if (icon != null) {
                this.iv = new ImageView(context);
                this.iv.setLayoutParams(new LayoutParams(-2, -2));
                this.iv.setImageDrawable(icon);
                addView(this.iv);
            }
            if (txt != null && txt.length() != 0) {
                this.tv = new TextView(context);
                this.tv.setLayoutParams(new LayoutParams(-1, -1));
                this.tv.setGravity(16);
                this.tv.setPadding(5, 0, 10, 0);
                this.tv.setTextSize(18.0f);
                this.tv.setText(txt);
                addView(this.tv);
            }
        }

        public void setEnabled(boolean enabled) {
            if (this.iv != null) {
                this.iv.setEnabled(enabled);
            }
            if (this.tv != null) {
                this.tv.setEnabled(enabled);
                this.tv.setTextColor(enabled ? -1 : -7829368);
            }
            super.setEnabled(enabled);
        }

        public boolean isLine() {
            return false;
        }
    }

    public static class VerticalLine extends PopoverWindowItem {
        ImageView line;

        public VerticalLine(Context context, Drawable icon, String txt, boolean lineElement) {
            super(context, icon, txt, lineElement);
            setOrientation(1);
            setLayoutParams(new LayoutParams(-2, -1));
            this.line = new ImageView(context);
            this.line.setMinimumWidth(1);
            this.line.setLayoutParams(new LayoutParams(-2, -1));
            this.line.setBackgroundColor(-15000288);
            addView(this.line);
        }

        public void setEnabled(boolean enabled) {
            if (this.line != null) {
                this.line.setEnabled(enabled);
            }
            super.setEnabled(enabled);
        }

        public boolean isLine() {
            return true;
        }
    }

    public abstract boolean isLine();

    public PopoverWindowItem(Context context, Drawable icon, String txt, boolean lineElement) {
        super(context);
    }
}

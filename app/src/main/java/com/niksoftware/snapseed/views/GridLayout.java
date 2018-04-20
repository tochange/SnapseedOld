package com.niksoftware.snapseed.views;

import android.content.Context;
import android.graphics.Point;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.GridLayoutAnimationController.AnimationParameters;

public class GridLayout extends ViewGroup {
    private Point _cellSize;
    private int _numColumns = 1;
    private int _numRows = 1;
    private int cell_X_Spacing = 10;
    private int cell_Y_Spacing = 20;

    public GridLayout(Context context) {
        super(context);
        setWillNotDraw(true);
    }

    public int getNumColumns() {
        return this._numColumns;
    }

    public int getNumRows() {
        return this._numRows;
    }

    public void setNumColumns(int columns) {
        setNumColumns(columns, true);
    }

    public void setNumRows(int rows) {
        setNumRows(rows, true);
    }

    public void setNumColumns(int columns, boolean requestLayout) {
        this._cellSize = null;
        this._numColumns = columns;
        if (requestLayout) {
            requestLayout();
        }
    }

    public void setNumRows(int rows, boolean requestLayout) {
        this._cellSize = null;
        this._numRows = rows;
        if (requestLayout) {
            requestLayout();
        }
    }

    public void setCellSpacing(int horz, int vert) {
        this.cell_X_Spacing = horz;
        this.cell_Y_Spacing = vert;
        requestLayout();
    }

    protected void attachLayoutAnimationParameters(View child, LayoutParams params, int index, int count) {
        AnimationParameters animationParams = params.layoutAnimationParameters;
        if (animationParams == null) {
            animationParams = new AnimationParameters();
            params.layoutAnimationParameters = animationParams;
        }
        animationParams.count = count;
        animationParams.index = index;
        animationParams.columnsCount = this._numColumns;
        animationParams.rowsCount = this._numRows;
        animationParams.column = index % this._numColumns;
        animationParams.row = index / this._numColumns;
    }

    protected Point getMaxItemSize() {
        Point result = new Point();
        for (int i = 0; i < getChildCount(); i++) {
            View itemView = getChildAt(i);
            itemView.measure(MeasureSpec.makeMeasureSpec(0, 0), MeasureSpec.makeMeasureSpec(0, 0));
            result.x = Math.max(result.x, itemView.getMeasuredWidth());
            result.y = Math.max(result.y, itemView.getMeasuredHeight());
        }
        return result;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (this._cellSize == null) {
            this._cellSize = getMaxItemSize();
        }
        setMeasuredDimension(MeasureSpec.makeMeasureSpec(((getPaddingLeft() + getPaddingRight()) + (this._numColumns * (this._cellSize.x + this.cell_X_Spacing))) - this.cell_X_Spacing, 1073741824), MeasureSpec.makeMeasureSpec(((getPaddingTop() + getPaddingBottom()) + ((((getChildCount() + this._numColumns) - 1) / this._numColumns) * (this._cellSize.y + this.cell_Y_Spacing))) - this.cell_Y_Spacing, 1073741824));
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (changed) {
            if (this._cellSize == null) {
                this._cellSize = getMaxItemSize();
            }
            int columns = this._numColumns;
            int paddingLeft = getPaddingLeft();
            int paddingTop = getPaddingTop();
            int count = getChildCount();
            for (int i = 0; i < count; i++) {
                View itemView = getChildAt(i);
                if (itemView.getVisibility() != 8) {
                    int column = i % columns;
                    int row = i / columns;
                    int itemWidth = itemView.getMeasuredWidth();
                    int itemHeight = itemView.getMeasuredHeight();
                    int itemLeft = (((this._cellSize.x + this.cell_X_Spacing) * column) + paddingLeft) + ((this._cellSize.x - itemWidth) / 2);
                    int itemTop = (((this._cellSize.y + this.cell_Y_Spacing) * row) + paddingTop) + ((this._cellSize.y - itemHeight) / 2);
                    itemView.layout(itemLeft, itemTop, itemLeft + itemWidth, itemTop + itemHeight);
                }
            }
        }
    }
}

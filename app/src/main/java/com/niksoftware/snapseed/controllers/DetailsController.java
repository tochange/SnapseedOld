package com.niksoftware.snapseed.controllers;

import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;
import android.view.View.OnClickListener;

import com.niksoftware.snapseed.controllers.touchhandlers.TouchHandler;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.util.Geometry;
import com.niksoftware.snapseed.views.BaseFilterButton;
import com.niksoftware.snapseed.views.LoupeView;
import com.niksoftware.snapseed.views.WorkingAreaView;

public class DetailsController extends EmptyFilterController {
    private static final int[] ADJUSTABLE_FILTER_PARAMETERS = new int[]{15, 16};
    private static final int LOUPE_AREA = 150;
    private NotificationCenterListener _didChangeCompareImageMode;
    private NotificationCenterListener _didChangeFilterParameterValue;
    private NotificationCenterListener _didChangeParameterViewVisibility;
    private int _diffX = 0;
    private int _diffY = 0;
    private LoupeView _loupe;
    private LoupeView _loupeBackup;
    private BaseFilterButton _loupeButton;
    private Point _loupeCenterPos = new Point();
    private Rect _loupeRect = new Rect();
    private boolean _showMenu = false;
    private float _xpos = 0.5f;
    private float _ypos = 0.5f;

    public void init(ControllerContext controllerContext) {
        super.init(controllerContext);
        addParameterHandler();
        addTouchListener(new TouchHandler() {
            public boolean handleTouchDown(float x, float y) {
                return DetailsController.this.startToMoveLoupe((int) x, (int) y);
            }

            public boolean handleTouchMoved(float x, float y) {
                DetailsController.this.moveLoupe((int) x, (int) y);
                return false;
            }
        });
        this._didChangeFilterParameterValue = new NotificationCenterListener() {
            public void performAction(Object arg) {
                if (DetailsController.this._loupe != null) {
                    DetailsController.this._loupe.updateImage();
                    DetailsController.this._loupe.setVisibility(8);
                    DetailsController.this._loupe.setVisibility(0);
                }
            }
        };
        NotificationCenter notificationCenter = NotificationCenter.getInstance();
        notificationCenter.addListener(this._didChangeFilterParameterValue, ListenerType.DidChangeFilterParameterValue);
        this._didChangeCompareImageMode = new NotificationCenterListener() {
            public void performAction(Object arg) {
                if (DetailsController.this._loupe != null) {
                    DetailsController.this._loupe.setCompareImageMode(((Boolean) arg).booleanValue());
                    DetailsController.this._loupe.updateImage();
                    DetailsController.this._loupe.setVisibility(8);
                    DetailsController.this._loupe.setVisibility(0);
                }
            }
        };
        notificationCenter.addListener(this._didChangeCompareImageMode, ListenerType.DidChangeCompareImageMode);
        this._didChangeParameterViewVisibility = new NotificationCenterListener() {
            public void performAction(Object arg) {
                DetailsController.this.onShowMenu(((Boolean) arg).booleanValue());
            }
        };
        notificationCenter.addListener(this._didChangeParameterViewVisibility, ListenerType.DidChangeParameterViewVisibility);
    }

    public void cleanup() {
        if (this._loupe != null) {
            getWorkingAreaView().removeView(this._loupe);
            this._loupe = null;
        }
        NotificationCenter notificationCenter = NotificationCenter.getInstance();
        notificationCenter.removeListener(this._didChangeFilterParameterValue, ListenerType.DidChangeFilterParameterValue);
        notificationCenter.removeListener(this._didChangeParameterViewVisibility, ListenerType.DidChangeParameterViewVisibility);
        notificationCenter.removeListener(this._didChangeCompareImageMode, ListenerType.DidChangeCompareImageMode);
    }

    private void onShowMenu(boolean visible) {
        this._showMenu = visible;
        if (visible) {
            this._loupeBackup = setLoupe(null);
            return;
        }
        if (this._loupeButton.isSelected() && this._loupeBackup != null) {
            setLoupe(this._loupeBackup);
        }
        this._loupeBackup = null;
    }

    public void onPause() {
        super.onPause();
        setLoupe(null);
        this._loupeButton.setSelected(false);
    }

    public void undoRedoStateChanged() {
        super.undoRedoStateChanged();
        if (this._loupe != null) {
            this._loupe.updateImage(this._loupeCenterPos.x, this._loupeCenterPos.y, true);
        }
    }

    private Point getLoupePosition() {
        View imageView = getWorkingAreaView().getImageView();
        return new Point((int) (this._xpos * ((float) imageView.getWidth())), (int) (this._ypos * ((float) imageView.getHeight())));
    }

    void xpos(int x) {
        this._xpos = ((float) x) / ((float) getWorkingAreaView().getImageView().getWidth());
        this._xpos = Math.min(1.0f, Math.max(this._xpos, 0.0f));
    }

    void ypos(int y) {
        this._ypos = ((float) y) / ((float) getWorkingAreaView().getImageView().getHeight());
        this._ypos = Math.min(1.0f, Math.max(this._ypos, 0.0f));
    }

    private LoupeView setLoupe(LoupeView loupe) {
        if (loupe != null) {
            if (this._showMenu) {
                this._loupeBackup = loupe;
            } else if (this._loupe == null) {
                this._loupe = loupe;
                getWorkingAreaView().addView(this._loupe);
                layoutLoupe();
            }
            return null;
        }
        if (this._loupe != null) {
            getWorkingAreaView().removeView(this._loupe);
        }
        LoupeView l = this._loupe;
        this._loupe = null;
        return l;
    }

    private boolean startToMoveLoupe(int x, int y) {
        if (this._loupe == null) {
            return false;
        }
        Point loupePos = getLoupePosition();
        int dist = (int) Geometry.distance((float) x, (float) y, (float) loupePos.x, (float) loupePos.y);
        if (dist > LOUPE_AREA) {
            this._diffY = dist;
            this._diffX = dist;
            return false;
        }
        this._diffX = loupePos.x - x;
        this._diffY = loupePos.y - y;
        return true;
    }

    private void moveLoupe(int mouseX, int mouseY) {
        if (this._loupe != null && this._diffX <= LOUPE_AREA && this._diffY <= LOUPE_AREA) {
            xpos(this._diffX + mouseX);
            ypos(this._diffY + mouseY);
            layoutLoupe();
        }
    }

    private void updateLoupeState() {
        Point currentPos = getLoupePosition();
        WorkingAreaView workingAreaView = getWorkingAreaView();
        Rect imageRect = workingAreaView.getImageViewScreenRect();
        currentPos.x = Math.min(Math.max(0, currentPos.x), imageRect.width() - 1);
        currentPos.y = Math.min(Math.max(0, currentPos.y), imageRect.height() - 1);
        this._loupeRect.set(currentPos.x - (this._loupe.getLoupeSize() / 2), currentPos.y - (this._loupe.getLoupeSize() / 2), currentPos.x + ((this._loupe.getLoupeSize() + 1) / 2), currentPos.y + ((this._loupe.getLoupeSize() + 1) / 2));
        this._loupeRect.offset(imageRect.left, imageRect.top);
        this._loupeCenterPos.x = (currentPos.x * workingAreaView.getImageWidth()) / workingAreaView.getImageView().getWidth();
        this._loupeCenterPos.y = (currentPos.y * workingAreaView.getImageHeight()) / workingAreaView.getImageView().getHeight();
    }

    private void layoutLoupe() {
        updateLoupeState();
        getRootView().forceLayoutForFilterGUI = true;
        getWorkingAreaView().requestLayout();
    }

    public int getFilterType() {
        return 13;
    }

    public int[] getGlobalAdjustmentParameters() {
        return ADJUSTABLE_FILTER_PARAMETERS;
    }

    public boolean initLeftFilterButton(BaseFilterButton button) {
        if (button == null) {
            this._loupeButton = null;
            return false;
        }
        this._loupeButton = button;
        this._loupeButton.setStateImages((int) R.drawable.icon_tb_loupe_default, (int) R.drawable.icon_tb_loupe_active, 0);
        this._loupeButton.setText(getButtonTitle(R.string.loupe));
        this._loupeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (DetailsController.this._loupeButton.isSelected()) {
                    DetailsController.this._loupeButton.setSelected(false);
                    DetailsController.this.setLoupe(null);
                    return;
                }
                DetailsController.this._loupeButton.setSelected(true);
                DetailsController.this.setLoupe(new LoupeView(DetailsController.this.getContext(), false, true));
            }
        });
        if (this._loupe != null) {
            this._loupeButton.setSelected(true);
        }
        return true;
    }

    public BaseFilterButton getLeftFilterButton() {
        return this._loupeButton;
    }

    public boolean showsParameterView() {
        return true;
    }

    public int getHelpResourceId() {
        return R.xml.overlay_details;
    }

    public void layout(boolean changed, int left, int top, int right, int bottom) {
        super.layout(changed, left, top, right, bottom);
        if (this._loupe != null) {
            updateLoupeState();
            this._loupe.layout(this._loupeRect.left, this._loupeRect.top, this._loupeRect.right, this._loupeRect.bottom);
            this._loupe.updateImage(this._loupeCenterPos.x, this._loupeCenterPos.y);
        }
    }
}

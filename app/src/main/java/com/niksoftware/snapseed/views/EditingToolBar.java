package com.niksoftware.snapseed.views;

import android.content.Context;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.niksoftware.snapseed.MainActivity;
import com.niksoftware.snapseed.R;
import com.niksoftware.snapseed.controllers.FilterController;
import com.niksoftware.snapseed.core.DeviceDefs;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.UndoManager;
import com.niksoftware.snapseed.core.UndoReceiver;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.views.PopoverWindow.OnActionItemClickListener;

public class EditingToolBar extends RelativeLayout {
    private static final long ACTION_BUTTON_LOCK_DELAY = 250;
    private static boolean _undoButtonEnabled = false;
    private ToolButton _applyButton;
    private ToolButton _backButton;
    private ToolButton _compareButton;
    protected boolean _enabled = false;
    private BaseFilterButton _filterContextButton_left;
    private BaseFilterButton _filterContextButton_right;
    private boolean _isTablet = DeviceDefs.isTablet();
    protected long _lastActionTime;
    protected NotificationCenterListener _myListener;
    private ScaleParameterDisplay _parameterDisplay;
    private PopoverWindowItem _redoPopoverItem;
    private ToolButton _undoButton;
    private PopoverWindowItem _undoPopoverItem;
    private PopoverWindow _undoPopoverWindow;

    private class EditingToolbarButtonListener implements OnClickListener {
        private boolean _goBack;

        public EditingToolbarButtonListener(boolean goBack) {
            this._goBack = goBack;
        }

        public void onClick(View v) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - EditingToolBar.this._lastActionTime > EditingToolBar.ACTION_BUTTON_LOCK_DELAY) {
                EditingToolBar.this._lastActionTime = currentTime;
                FilterController controller = MainActivity.getMainActivity().getFilterController();
                if (controller != null && controller.getFilterType() != 1) {
                    if (this._goBack) {
                        controller.onCancelFilter();
                    } else {
                        controller.onApplyFilter();
                    }
                }
            }
        }
    }

    private class UndoClickListener implements OnClickListener {
        private UndoClickListener() {
        }

        public void onClick(View v) {
            UndoManager undoManager = UndoManager.getUndoManager();
            if (!undoManager.canUndo() && !undoManager.canRedo()) {
                return;
            }
            if (undoManager.canUndo() || !undoManager.canRedo()) {
                undoManager.makeUndo(MainActivity.getWorkingAreaView());
            } else {
                EditingToolBar.this.ShowUndoPopoverWindow();
            }
        }
    }

    private class UndoLongClickListener implements OnLongClickListener {
        private UndoLongClickListener() {
        }

        public boolean onLongClick(View v) {
            EditingToolBar.this.ShowUndoPopoverWindow();
            return true;
        }
    }

    protected EditingToolBar(RelativeLayout inflatedLayout) {
        super(inflatedLayout.getContext());
        setLayoutParams(new LayoutParams(-1, -2));
        addView(inflatedLayout, new LayoutParams(-1, -2));
        int maxWidth = getResources().getDimensionPixelSize(R.dimen.tb_button_title_max_width);
        this._backButton = (ToolButton) findViewById(R.id.back_button);
        this._backButton.setOnClickListener(new EditingToolbarButtonListener(true));
        this._backButton.setMaxTextWidth(maxWidth, TruncateAt.END);
        this._applyButton = (ToolButton) findViewById(R.id.apply_button);
        this._applyButton.setOnClickListener(new EditingToolbarButtonListener(false));
        this._applyButton.setMaxTextWidth(maxWidth, TruncateAt.END);
        this._undoButton = (ToolButton) findViewById(R.id.undo_button);
        if (this._undoButton != null) {
            this._undoButton.setOnClickListener(new UndoClickListener());
            this._undoButton.setLongClickable(true);
            this._undoButton.setEnabled(false);
            this._undoButton.setOnLongClickListener(new UndoLongClickListener());
            this._undoButton.setMaxTextWidth(maxWidth, TruncateAt.END);
        }
        this._compareButton = (ToolButton) findViewById(R.id.compare_button);
        if (this._compareButton != null) {
            this._compareButton.setMaxTextWidth(maxWidth, TruncateAt.END);
        }
        this._parameterDisplay = (ScaleParameterDisplay) findViewById(R.id.scale_param_display);
        this._filterContextButton_left = (BaseFilterButton) findViewById(R.id.left_filter_button);
        if (this._filterContextButton_left != null) {
            this._filterContextButton_left.setMaxTextWidth(maxWidth, TruncateAt.END);
        }
        this._filterContextButton_right = (BaseFilterButton) findViewById(R.id.right_filter_button);
        if (this._filterContextButton_right != null) {
            this._filterContextButton_right.setMaxTextWidth(maxWidth, TruncateAt.END);
        }
        setEnabled(false);
        NotificationCenter instance = NotificationCenter.getInstance();
        NotificationCenterListener anonymousClass1 = new NotificationCenterListener() {
            public void performAction(Object arg) {
                EditingToolBar.this.updateFilterController(MainActivity.getMainActivity().getFilterController());
            }
        };
        this._myListener = anonymousClass1;
        instance.addListener(anonymousClass1, ListenerType.DidCreateFilterGUI);
    }

    public void cleanup() {
        NotificationCenter.getInstance().removeListener(this._myListener, ListenerType.DidCreateFilterGUI);
        this._parameterDisplay.cleanup();
    }

    public static EditingToolBar createEditingToolbar(Context context) {
        return new EditingToolBar((RelativeLayout) ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.editing_toolbar, null));
    }

    public void updateFilterController(FilterController controller) {
        initWithFilter(null, controller);
    }

    public void setUndoButtonSelectionState(boolean selected) {
        if (this._undoButton != null) {
            this._undoButton.setEnabled(selected);
            _undoButtonEnabled = selected;
        }
    }

    private void initWithFilter(FilterParameter parameter, FilterController controller) {
        int i;
        int i2 = 8;
        BaseFilterButton baseFilterButton = this._filterContextButton_left;
        if (controller.initLeftFilterButton(this._filterContextButton_left)) {
            i = 0;
        } else {
            i = 8;
        }
        baseFilterButton.setVisibility(i);
        BaseFilterButton baseFilterButton2 = this._filterContextButton_right;
        if (controller.initRightFilterButton(this._filterContextButton_right)) {
            i2 = 0;
        }
        baseFilterButton2.setVisibility(i2);
    }

    public View getApplyButton() {
        return this._applyButton;
    }

    public View getBackButton() {
        return this._backButton;
    }

    public View getCompareButton() {
        return this._compareButton;
    }

    public BaseFilterButton getFilterButtonLeft() {
        return this._filterContextButton_left;
    }

    public BaseFilterButton getFilterButtonRight() {
        return this._filterContextButton_right;
    }

    public void setCompareLabelText(int textResource) {
        if (this._compareButton != null) {
            this._compareButton.setText(textResource);
        }
    }

    public boolean getEnabled() {
        return this._enabled;
    }

    public void setEnabled(boolean enabled) {
        this._enabled = enabled;
        setBackEnabled(enabled);
        setCompareEnabled(enabled);
        setUndoEnabled(enabled);
        setOkEnabled(enabled);
        setFilterControlsEnabled(enabled);
    }

    public void setEnabledResume() {
        boolean enabled;
        boolean z = true;
        if (this._compareButton == null || !this._compareButton.isPressed()) {
            enabled = true;
        } else {
            enabled = false;
        }
        setCompareEnabled(true);
        setBackEnabled(enabled);
        if (!(enabled && _undoButtonEnabled)) {
            z = false;
        }
        setUndoEnabled(z);
        setOkEnabled(enabled);
        setFilterControlsEnabled(enabled);
    }

    public void setBackEnabled(boolean enabled) {
        this._backButton.setEnabled(enabled);
    }

    public void setCompareEnabled(boolean enabled) {
        if (this._compareButton != null) {
            this._compareButton.setEnabled(enabled);
        }
    }

    public void setCompareHidden(boolean hidden) {
        if (this._compareButton != null && this._isTablet) {
            this._compareButton.setVisibility(hidden ? 4 : 0);
        }
    }

    public void setUndoEnabled(boolean enabled) {
        if (this._undoButton != null && _undoButtonEnabled) {
            this._undoButton.setEnabled(enabled);
        }
    }

    public void setUndoHidden(boolean hidden) {
        if (this._undoButton != null && this._isTablet) {
            this._undoButton.setVisibility(hidden ? 4 : 0);
        }
    }

    private void setOkEnabled(boolean enabled) {
        this._applyButton.setEnabled(enabled);
    }

    public void setFilterControlsEnabled(boolean enabled) {
        setContextButtonEnabled(enabled, enabled);
    }

    public void setContextButtonEnabled(boolean leftButtonEnabled, boolean rightButtonEnabled) {
        if (this._filterContextButton_left != null) {
            this._filterContextButton_left.setEnabled(leftButtonEnabled);
            if (!leftButtonEnabled) {
                this._filterContextButton_left.setSelected(false);
            }
        }
        if (this._filterContextButton_right != null) {
            this._filterContextButton_right.setEnabled(rightButtonEnabled);
            if (!rightButtonEnabled) {
                this._filterContextButton_right.setSelected(false);
            }
        }
    }

    public void cleanupFilterControls() {
        this._parameterDisplay.setVisibility(4);
    }

    public void addGlobalFilterControls() {
        this._parameterDisplay.setVisibility(0);
    }

    public void itemSelectorWillHide() {
    }

    private void updateUndoRedoItems() {
        UndoManager undoManager = UndoManager.getUndoManager();
        if (this._undoPopoverItem != null) {
            this._undoPopoverItem.setEnabled(undoManager.canUndo());
        }
        if (this._redoPopoverItem != null) {
            this._redoPopoverItem.setEnabled(undoManager.canRedo());
        }
    }

    private void ShowUndoPopoverWindow() {
        this._undoPopoverWindow = new PopoverWindow(MainActivity.getMainActivity(), 1);
        UndoManager undoManager = UndoManager.getUndoManager();
        if (undoManager.canUndo() || undoManager.canRedo()) {
            this._undoPopoverItem = this._undoPopoverWindow.addItem(null, getContext().getString(R.string.undo), undoManager.canUndo());
            this._redoPopoverItem = this._undoPopoverWindow.addItem(null, getContext().getString(R.string.redo), undoManager.canRedo());
            this._undoPopoverWindow.setOnActionItemClickListener(new OnActionItemClickListener() {
                public void onItemClick(int pos) {
                    UndoManager undoManager = UndoManager.getUndoManager();
                    UndoReceiver receiver = MainActivity.getWorkingAreaView();
                    if (pos == 0) {
                        undoManager.makeUndo(receiver);
                    } else if (pos == 1) {
                        undoManager.makeRedo(receiver);
                    }
                    EditingToolBar.this.updateUndoRedoItems();
                }
            });
            this._undoPopoverWindow.show(this._undoButton);
        }
    }

    public void hideUndoPopoverWindow() {
        if (this._undoPopoverWindow != null) {
            this._undoPopoverWindow.dismiss();
        }
    }

    public void resetStyleButtons() {
        itemSelectorWillHide();
    }

    public void setStyleButtons(boolean isLeft, boolean isRight) {
        if (!isRight || !isLeft) {
            if (this._filterContextButton_left != null && isLeft) {
                this._filterContextButton_left.setSelected(true);
            }
            if (this._filterContextButton_right != null && isRight) {
                this._filterContextButton_right.setSelected(true);
            }
        }
    }

    public void setPreviewShown(boolean previewShown) {
        if (previewShown) {
            setFilterControlsEnabled(false);
            this._backButton.setEnabled(false);
            if (this._undoButton != null) {
                this._undoButton.setEnabled(false);
            }
            this._applyButton.setEnabled(false);
            return;
        }
        setFilterControlsEnabled(true);
        this._backButton.setEnabled(true);
        if (_undoButtonEnabled && this._undoButton != null) {
            this._undoButton.setEnabled(true);
        }
        this._applyButton.setEnabled(true);
    }
}

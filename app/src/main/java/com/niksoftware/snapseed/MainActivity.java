package com.niksoftware.snapseed;

import android.animation.Animator;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.Window;
import android.widget.ImageView;
import com.niksoftware.snapseed.controllers.ControllerContext;
import com.niksoftware.snapseed.controllers.ControllerContext.Builder;
import com.niksoftware.snapseed.controllers.CropController;
import com.niksoftware.snapseed.controllers.FilterController;
import com.niksoftware.snapseed.controllers.FilterControllerFactory;
import com.niksoftware.snapseed.controllers.StraightenController;
import com.niksoftware.snapseed.core.DeviceDefs;
import com.niksoftware.snapseed.core.EditSession;
import com.niksoftware.snapseed.core.EditSessionInterface.LoadImageListener;
import com.niksoftware.snapseed.core.EditSessionInterface.OperationResult;
import com.niksoftware.snapseed.core.EditSessionInterface.RemoveFilterListener;
import com.niksoftware.snapseed.core.EditSessionInterface.RevertImageListener;
import com.niksoftware.snapseed.core.EditSessionInterface.SaveImageListener;
import com.niksoftware.snapseed.core.FilterDefs.FilterType;
import com.niksoftware.snapseed.core.NativeCore;
import com.niksoftware.snapseed.core.NotificationCenter;
import com.niksoftware.snapseed.core.NotificationCenterListener;
import com.niksoftware.snapseed.core.NotificationCenterListener.ListenerType;
import com.niksoftware.snapseed.core.RenderFilterInterface.OnPreviewRenderListener;
import com.niksoftware.snapseed.core.RenderFilterInterface.OnRenderListener;
import com.niksoftware.snapseed.core.Settings;
import com.niksoftware.snapseed.core.SnapseedAppDelegate;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.util.OfflineTracker;
import com.niksoftware.snapseed.util.SharingHelper;
import com.niksoftware.snapseed.util.TrackerData;
import com.niksoftware.snapseed.util.TrackerInterface;
import com.niksoftware.snapseed.views.ActionView;
import com.niksoftware.snapseed.views.EditingToolBar;
import com.niksoftware.snapseed.views.GlobalToolBar;
import com.niksoftware.snapseed.views.ImageViewGL;
import com.niksoftware.snapseed.views.RootView;
import com.niksoftware.snapseed.views.WorkingAreaView;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class MainActivity extends Activity implements LoadImageListener, SaveImageListener, RevertImageListener, RemoveFilterListener, OnSharedPreferenceChangeListener {
    static final /* synthetic */ boolean $assertionsDisabled = (!MainActivity.class.desiredAssertionStatus() ? true : false);
    private static final int ABOUT_SNAPSEED_ID = 7;
    private static final int ALPHA_30_PERCENT_TRANSPARENT = 64;
    private static final int ALPHA_OPAQUE = 255;
    private static final String CAMERA_FILE_NAME = "captured_by_snapseed";
    private static final int CAPTURE_IMAGE_ACTIVITY_ID = 2;
    private static final String GETPROP_HEAPSIZE_COMMAND = "getprop dalvik.vm.heapsize";
    private static final int HEAP_SIZE_THRESHOLD = 256;
    private static final int IMAGE_16MP_PIXEL_COUNT = 16777216;
    private static final int IMAGE_PROPERTY_ID = 8;
    private static final int MIN_IMAGE_EDGE_SIZE = 32;
    private static final int ONLINE_HELP_ID = 3;
    private static final int OPEN_IMAGE_ID = 18;
    private static final int PICK_IMAGE_ACTIVITY_ID = 1;
    public static final String PREFS_NAME = "SnapseedPrefs";
    private static final int RESIZE_IMAGE_MESSAGE_HIDE_DELAY = 2500;
    private static final int SAVE_IMAGE_ID = 17;
    private static final int SHARE_IMAGE_GOOGLE_PLUS_ID = 15;
    private static final int SHARE_IMAGE_ID = 16;
    private static final int SHOW_OVERLAY_ID = 2;
    private static final int SHOW_SYS_INFO_ID = 12;
    private static final int THRESHOLD_SMALLER_WIDTH = 800;
    private static MainActivity _mainActivity;
    private AlertDialog _activeDialog;
    private boolean _callsCameraIntent = $assertionsDisabled;
    private Uri _cameraImageUri;
    private EditSession _editSession;
    private FilterController _filterController;
    private boolean _isPaused = $assertionsDisabled;
    private MenuItem _menuAboutSnapseed;
    private MenuItem _menuImageProperty;
    private MenuItem _menuOnlineHelp;
    private MenuItem _menuOnlineVideo;
    private MenuItem _menuShowOverlay;
    public int _newFilterType;
    private MenuItem _openItem;
    private RootView _rootView;
    private MenuItem _saveToLibrary;
    private MenuItem _shareItem;
    private MenuItem _shareToGooglePlus;
    private Screen mCurrentScreen = Screen.MAIN;
    private long mStartTimeOfFilter;
    private WorkingAreaView workingAreaView;

    private class CompareOnTouchListener implements OnTouchListener {
        private CompareOnTouchListener() {
        }

        public boolean onTouch(View view, MotionEvent event) {
            int action = event.getAction();
            if (!view.isEnabled() || (action != 3 && action != 1 && action != 0)) {
                return MainActivity.$assertionsDisabled;
            }
            boolean compareOn = action == 0 ? true : MainActivity.$assertionsDisabled;
            MainActivity.this.workingAreaView.setEnabled(!compareOn ? true : MainActivity.$assertionsDisabled);
            view.setSelected(compareOn);
            int filterType = MainActivity.this.workingAreaView.getActiveFilterType();
            if (filterType != 1) {
                MainActivity.this.switchEditScreenCompareMode(filterType, compareOn);
                return true;
            } else if (!MainActivity.this._editSession.hasChanges()) {
                return MainActivity.$assertionsDisabled;
            } else {
                switch (event.getAction()) {
                    case 0:
                        MainActivity.this.switchMainScreenCompareMode(true);
                        return true;
                    case 1:
                    case 3:
                        MainActivity.this.switchMainScreenCompareMode(MainActivity.$assertionsDisabled);
                        return true;
                    default:
                        return true;
                }
            }
        }
    }

    private class ExportOnRenderListener implements OnRenderListener {
        private boolean showsProgress;

        private ExportOnRenderListener() {
        }

        public void onRenderProgressUpdate(int currentStage, int stageCount) {
            if (currentStage == 0) {
                this.showsProgress = stageCount > 2 ? true : MainActivity.$assertionsDisabled;
            }
            if (!this.showsProgress || MainActivity.this._isPaused) {
                return;
            }
            if (currentStage == 0) {
                SnapseedAppDelegate.getInstance().progressStart((int) R.string.processing);
            } else {
                SnapseedAppDelegate.getInstance().progressSetValue((currentStage * 100) / stageCount);
            }
        }

        public void onRenderCancelled() {
            reset();
        }

        public void onRenderFinished(Bitmap filteredImage) {
            reset();
            if (filteredImage == null) {
                throw new IllegalStateException("Invalid filtered image reference");
            }
            MainActivity.this.onExportFilteredImage(filteredImage);
        }

        private void reset() {
            if (this.showsProgress) {
                if (!MainActivity.this._isPaused) {
                    SnapseedAppDelegate.getInstance().progressEnd();
                }
                this.showsProgress = MainActivity.$assertionsDisabled;
            }
        }
    }

    private class MigrateStorageAsyncTask extends AsyncTask<Context, Void, Void> {
        private MigrateStorageAsyncTask() {
        }

        protected Void doInBackground(Context... contexts) {
            boolean z = MainActivity.$assertionsDisabled;
            Context context = contexts[0];
            if (!EditSession.migrateStorageFolder(context)) {
                z = true;
            }
            Settings.setNeedsMigrateStorage(context, z);
            return null;
        }
    }

    private class OpenUriListener implements OnClickListener {
        private Uri _imageUri;

        public OpenUriListener(Uri imageUri) {
            this._imageUri = imageUri;
        }

        public void onClick(DialogInterface dialogInterface, int which) {
            if (which != -2) {
                MainActivity.this.activateMainScreen(true);
                MainActivity.this._editSession.loadImage(MainActivity.this, this._imageUri, MainActivity.this);
            }
        }
    }

    private class RevertButtonListener implements View.OnClickListener {
        private RevertButtonListener() {
        }

        public void onClick(View v) {
            MainActivity.this.onRevertImage();
        }
    }

    private class SaveButtonListener implements View.OnClickListener {
        private SaveButtonListener() {
        }

        public void onClick(View v) {
            MainActivity.this.onSaveImage();
        }
    }

    public enum Screen {
        MAIN,
        EDIT_CONTROLS_LEFT,
        EDIT_CONTROLS_RIGHT
    }

    private class ShareButtonListener implements View.OnClickListener {
        private ShareButtonListener() {
        }

        public void onClick(View v) {
            int activeFilterType = MainActivity.this.workingAreaView.getActiveFilterType();
            if (activeFilterType == 1000 || activeFilterType == 1) {
                MainActivity.this.showShareDialog(true);
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TrackerData.initialize(createTracker());
        SnapseedAppDelegate.getInstance();
        DeviceDefs.initialize(this, getWindowManager());
        _mainActivity = this;
        Window window = getWindow();
        window.setFormat(3);
        window.requestFeature(9);
        this._rootView = new RootView(this);
        setContentView(this._rootView);
        this.workingAreaView = this._rootView.getWorkingAreaView();
        this.workingAreaView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                boolean z = true;
                FilterParameter filterParameter = MainActivity.getFilterParameter();
                if (filterParameter == null || filterParameter.getFilterType() == 1) {
                    return MainActivity.$assertionsDisabled;
                }
                if (event.getPointerCount() >= 1 && event.getAction() == 0) {
                    View editingToolbar = MainActivity.getEditingToolbar();
                    if (editingToolbar == null || ((float) editingToolbar.getTop()) < event.getY()) {
                        return MainActivity.$assertionsDisabled;
                    }
                }
                FilterController controller = MainActivity.this.getFilterController();
                if (controller == null || !controller.onTouch(v, event)) {
                    z = MainActivity.$assertionsDisabled;
                }
                return z;
            }
        });
        setUpGlobalToolBarListeners();
        ViewTreeObserver treeObserver = this._rootView.getViewTreeObserver();
        if (treeObserver != null) {
            treeObserver.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    if (Settings.getIsFirstRun(MainActivity.this)) {
                        Settings.setIsFirstRun(MainActivity.this, MainActivity.$assertionsDisabled);
                        MainActivity.this.showHelpOverlay();
                    }
                    ViewTreeObserver treeObserver = MainActivity.this._rootView.getViewTreeObserver();
                    if (treeObserver != null) {
                        treeObserver.removeGlobalOnLayoutListener(this);
                    }
                }
            });
        }
        window.setBackgroundDrawable(null);
        boolean isTablet = DeviceDefs.isTablet();
        ActionBar actionBar = getActionBar();
        if (actionBar == null) {
            throw new IllegalStateException();
        }
        actionBar.setDisplayShowTitleEnabled($assertionsDisabled);
        if (isTablet) {
            actionBar.setDisplayUseLogoEnabled(true);
            actionBar.setLogo(R.drawable.icon_logo_default);
        } else {
            actionBar.setDisplayShowCustomEnabled(true);
            ImageView logo = (ImageView) findViewById(16908332);
            if (logo != null) {
                logo.setImageResource(R.drawable.icon_logo_default);
            }
        }
        actionBar.show();
        activateMainScreen(true);
        NotificationCenter notificationCenter = NotificationCenter.getInstance();
        notificationCenter.addListener(new NotificationCenterListener() {
            public void performAction(Object arg) {
                if (MainActivity.this._shareItem != null) {
                    MainActivity.this._shareItem.setVisible(true);
                }
            }
        }, ListenerType.DidEnterMainScreen);
        notificationCenter.addListener(new NotificationCenterListener() {
            public void performAction(Object arg) {
                if (MainActivity.this._shareItem != null) {
                    MainActivity.this._shareItem.setVisible(MainActivity.$assertionsDisabled);
                }
                int filterType = MainActivity.this.getFilterController().getFilterType();
                if (Settings.getNeedsShowHelp(MainActivity.this, filterType)) {
                    MainActivity.this.showHelpOverlay();
                    Settings.setNeedsShowHelp(MainActivity.this, filterType, MainActivity.$assertionsDisabled);
                }
            }
        }, ListenerType.DidEnterEditingScreen);
        notificationCenter.addListener(new NotificationCenterListener() {
            public void performAction(Object arg) {
                boolean z = true;
                int filterType = ((Integer) arg).intValue();
                if (MainActivity.this._filterController != null) {
                    if (MainActivity.this._filterController.getFilterType() != filterType) {
                        MainActivity.this._filterController.cleanup();
                    } else {
                        return;
                    }
                }
                Builder contextBuilder = new Builder();
                contextBuilder.put(ControllerContext.EDIT_SESSION, MainActivity.this._editSession);
                contextBuilder.put(ControllerContext.ROOT_VIEW, MainActivity.this._rootView);
                contextBuilder.put(ControllerContext.WORKING_AREA_VIEW, MainActivity.this.workingAreaView);
                contextBuilder.put(ControllerContext.EDITING_TOOL_BAR, MainActivity.getEditingToolbar());
                MainActivity.this._filterController = FilterControllerFactory.createFilterController(contextBuilder.createContext(), filterType);
                if (filterType != 1 && filterType != 1000) {
                    boolean compareVisible = MainActivity.this._filterController != null ? true : MainActivity.$assertionsDisabled;
                    EditingToolBar editingToolbar = MainActivity.getEditingToolbar();
                    editingToolbar.setCompareHidden(!compareVisible ? true : MainActivity.$assertionsDisabled);
                    editingToolbar.setCompareLabelText(FilterType.isCPUFilter(filterType) ? R.string.preview : R.string.compare_btn);
                    boolean undoVisible = (compareVisible || filterType == 5 || filterType == 6) ? true : MainActivity.$assertionsDisabled;
                    if (undoVisible) {
                        z = MainActivity.$assertionsDisabled;
                    }
                    editingToolbar.setUndoHidden(z);
                    if (MainActivity.this._filterController.showsParameterView()) {
                        MainActivity.this.workingAreaView.updateParameterView(MainActivity.this.getFilterController().getGlobalAdjustmentParameters());
                    } else {
                        MainActivity.this.workingAreaView.removeParameterView();
                    }
                    if (MainActivity.this._filterController.useActionView()) {
                        MainActivity.this.workingAreaView.addActionView();
                    } else {
                        MainActivity.this.workingAreaView.removeActionView();
                    }
                    NotificationCenter.getInstance().performAction(ListenerType.DidCreateFilterGUI, null);
                }
            }
        }, ListenerType.DidActivateFilter);
        int maxImagePixelCount = getResources().getInteger(R.integer.max_image_pixel_count);
        if (getDalvikHeapSize() >= HEAP_SIZE_THRESHOLD) {
            maxImagePixelCount = IMAGE_16MP_PIXEL_COUNT;
        }
        this._editSession = new EditSession(getSampleImageResId(), getMaxScreenEdgeSize(), MIN_IMAGE_EDGE_SIZE, maxImagePixelCount, new Handler(getMainLooper()), getContentResolver());
        Uri imageUri = getImageUriFromIntent(getIntent());
        if (imageUri != null) {
            this._editSession.loadImage(this, imageUri, this);
        } else {
            boolean loadSampleImage = true;
            if (savedInstanceState != null) {
                loadSampleImage = !this._editSession.restoreState(this, savedInstanceState) ? true : $assertionsDisabled;
            }
            if (loadSampleImage) {
                this._editSession.loadImage(this, null, null);
            }
            this.workingAreaView.setImage(this._editSession.getCurrentImage(), this._editSession.getCurrentScreenImage());
        }
        if (Settings.getNeedsMigrateStorage(this)) {
            MainActivity mainActivity = this;
            new MigrateStorageAsyncTask().execute(new Context[]{this});
        }
    }

    private TrackerInterface createTracker() {
        return new OfflineTracker();
    }

    public void onStart() {
        super.onStart();
        TrackerData.getInstance().sendView(TrackerData.VIEW_MAIN_SCREEN);
    }

    protected void onResume() {
        super.onResume();
        NativeCore.INSTANCE.initContext(this);
        SnapseedAppDelegate.getInstance().progressEnd();
        if (this._rootView != null) {
            if (this.mStartTimeOfFilter < 0) {
                this.mStartTimeOfFilter += System.nanoTime();
            }
            this._rootView.closeAllPopovers();
        }
        this.workingAreaView.onResume();
        if (this._filterController != null) {
            this._filterController.onResume();
        }
        this._rootView.requestLayout();
        this._isPaused = $assertionsDisabled;
    }

    protected void onPause() {
        super.onPause();
        if (this._filterController != null && this._filterController.isApplyingFilter()) {
            this._filterController.resetApplyFilter();
        }
        this._isPaused = true;
        if (this._rootView != null) {
            if (this.mStartTimeOfFilter > 0) {
                this.mStartTimeOfFilter = -1 * (System.nanoTime() - this.mStartTimeOfFilter);
            }
            this._rootView.closeAllPopovers();
            this._rootView.hideHelpOverlay($assertionsDisabled);
        }
        if (this._activeDialog != null) {
            this._activeDialog.dismiss();
            this._activeDialog = null;
        }
        this.workingAreaView.onPause();
        if (this._filterController != null) {
            this._filterController.onPause();
        }
        NativeCore.getInstance().cleanupContext();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Uri imageUri = getImageUriFromIntent(intent);
        if (imageUri != null) {
            askToOpenNewImage(new OpenUriListener(imageUri));
        }
    }

    protected void onDestroy() {
        SnapseedAppDelegate.destroyInstance();
        NotificationCenter.destoryInstance();
        _mainActivity = null;
        super.onDestroy();
    }

    public void onDetachedFromWindow() {
        if (this._rootView != null) {
            this._rootView.closeAllPopovers();
        }
        super.onDetachedFromWindow();
    }

    public void onBackPressed() {
        if (!this._rootView.isRunningAnimation()) {
            if (this._filterController != null && this._filterController.isApplyingFilter()) {
                return;
            }
            if (this._filterController == null || this._filterController.getFilterType() == 1) {
                Intent setIntent = new Intent("android.intent.action.MAIN");
                setIntent.addCategory("android.intent.category.HOME");
                setIntent.setFlags(268435456);
                startActivity(setIntent);
                return;
            }
            this._filterController.onCancelFilter();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        if (this._openItem != null) {
            this._openItem.setTitle(R.string.dialog_open_photo);
        }
        if (this._saveToLibrary != null) {
            this._saveToLibrary.setTitle(R.string.save_btn);
        }
        if (this._shareToGooglePlus != null) {
            this._shareToGooglePlus.setTitle(R.string.google_plus_btn);
        }
        if (this._shareItem != null) {
            this._shareItem.setTitle(R.string.share_btn);
        }
        if (this._menuShowOverlay != null) {
            this._menuShowOverlay.setTitle(R.string.menu_show_overlay);
        }
        if (this._menuOnlineHelp != null) {
            this._menuOnlineHelp.setTitle(R.string.menu_online_help);
        }
        if (this._menuOnlineVideo != null) {
            this._menuOnlineVideo.setTitle(R.string.menu_online_videos);
        }
        if (this._menuAboutSnapseed != null) {
            this._menuAboutSnapseed.setTitle(R.string.menu_about);
        }
        if (this._menuImageProperty != null) {
            this._menuImageProperty.setTitle(R.string.image);
        }
        if (this._rootView != null) {
            GlobalToolBar globalToolBar = this._rootView.getGlobalToolBar();
            if (globalToolBar != null) {
                globalToolBar.refreshButtonTitles();
            }
            this._rootView.closeAllPopovers();
            this._rootView.hideHelpOverlay($assertionsDisabled);
            this._rootView.requestLayout();
            this._rootView.reloadEditingToolbar();
        }
        super.onConfigurationChanged(newConfig);
    }

    public Editor getSharedPrefEditor() {
        return getSharedPreferences(PREFS_NAME, 0).edit();
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this._editSession != null) {
            this._editSession.saveState(this, outState);
        }
    }

    public void onLoadStarted(EditSession editSession) {
        TrackerData.getInstance().newSessionBecauseOfNewImage();
        this.workingAreaView.willSetImage(true);
        SnapseedAppDelegate.getInstance().progressStartIndeterminate((int) R.string.progress_loading);
    }

    public void onLoadFinished(EditSession editSession, OperationResult resultCode, int sourceWidth, int sourceHeight) {
        getWorkingAreaView().setImage(editSession.getCurrentImage(), editSession.getCurrentScreenImage());
        SnapseedAppDelegate.getInstance().progressEnd();
        switch (resultCode) {
            case ERROR_COULD_NOT_OPEN:
            case ERROR_COULD_NOT_DECODE:
                showAlertDialogWithMessage(R.string.could_not_load);
                break;
            case ERROR_EXTREME_ASPECT_RATIO:
                showAlertDialogWithMessage(R.string.could_not_load__extreme_aspect_ratio);
                break;
            default:
                int imageWidth = editSession.getOriginalImageWidth();
                int imageHeight = editSession.getOriginalImageHeight();
                boolean imageWasShrunk = (sourceWidth == imageWidth && sourceHeight == imageHeight) ? $assertionsDisabled : true;
                if (imageWasShrunk) {
                    long j;
                    TrackerData instance = TrackerData.getInstance();
                    String str = "Shrink image";
                    String format = String.format("Original Size: %d x %d", new Object[]{Integer.valueOf(sourceWidth), Integer.valueOf(sourceHeight)});
                    String format2 = String.format("New Size: %d x %d", new Object[]{Integer.valueOf(Math.max(imageWidth, imageHeight)), Integer.valueOf(Math.min(imageWidth, imageHeight))});
                    if (imageWidth >= imageHeight) {
                        j = 1;
                    } else {
                        j = 0;
                    }
                    instance.sendEvent(str, format, format2, j);
                } else {
                    TrackerData.getInstance().sendEvent("Image size", String.format("Original Size: %d x %d", new Object[]{Integer.valueOf(Math.max(imageWidth, imageHeight)), Integer.valueOf(Math.min(imageWidth, imageHeight))}), "", imageWidth >= imageHeight ? 1 : 0);
                }
                if (imageWasShrunk) {
                    showAlertDialogWithMessage(String.format(getString(R.string.will_strink_image_message), new Object[]{Integer.valueOf(editSession.getOriginalImageWidth()), Integer.valueOf(editSession.getOriginalImageHeight())}), RESIZE_IMAGE_MESSAGE_HIDE_DELAY);
                    break;
                }
                break;
        }
        invalidateOptionsMenu();
    }

    public void onSaveStarted(EditSession editSession) {
        SnapseedAppDelegate.getInstance().progressStartIndeterminate((int) R.string.saving_image);
    }

    public void onSaveFinished(EditSession editSession, OperationResult resultCode, Uri savedUri) {
        SnapseedAppDelegate.getInstance().progressEnd();
        switch (resultCode) {
            case ERROR_SAVE_FAILED:
                AlertDialog dlg1 = new AlertDialog.Builder(this).create();
                dlg1.setMessage(getString(R.string.could_not_save));
                dlg1.show();
                break;
            case ERROR_SAVE_SD_NOT_WRITABLE:
                AlertDialog dlg2 = new AlertDialog.Builder(this).create();
                dlg2.setMessage(getString(R.string.no_writable_sd_card));
                dlg2.show();
                break;
            default:
                ActionView actionView = this.workingAreaView.getActionView();
                if (actionView != null) {
                    actionView.setMessage((int) R.string.successfully_saved);
                }
                TrackerData.getInstance().sendDataImagesSaved();
                break;
        }
        invalidateOptionsMenu();
    }

    public void onRemoveFilterStarted(EditSession editSession) {
        onRevertStarted(editSession);
    }

    public void onRemoveFilterFinished(EditSession editSession, OperationResult resultCode) {
        onRevertFinished(editSession, resultCode);
    }

    public void onRevertStarted(EditSession editSession) {
        lockCurrentOrientation();
        this.workingAreaView.willSetImage(true);
        SnapseedAppDelegate.getInstance().progressStartIndeterminate((int) R.string.reverting_image);
    }

    public void onRevertFinished(EditSession editSession, OperationResult resultCode) {
        if (resultCode == OperationResult.SUCCESS) {
            TrackerData.getInstance().revertImage();
        }
        this.workingAreaView.setImage(editSession.getCurrentImage(), editSession.getCurrentScreenImage());
        SnapseedAppDelegate.getInstance().progressEnd();
        unlockCurrentOrientation();
        invalidateOptionsMenu();
    }

    public EditSession getEditSession() {
        return this._editSession;
    }

    private Uri getImageUriFromIntent(Intent intent) {
        if (intent == null) {
            return null;
        }
        String action = intent.getAction();
        if (action == null) {
            return null;
        }
        if (action.equals("android.intent.action.EDIT")) {
            return intent.getData();
        }
        if (action.equals("android.intent.action.SEND")) {
            return (Uri) intent.getExtras().get("android.intent.extra.STREAM");
        }
        return null;
    }

    public void showShareDialog(final boolean shareToGooglePlus) {
        if (needsSave()) {
            this._editSession.saveImage(this, new SaveImageListener() {
                public void onSaveStarted(EditSession editSession) {
                    SnapseedAppDelegate.getInstance().progressStartIndeterminate((int) R.string.saving_image);
                }

                public void onSaveFinished(EditSession editSession, OperationResult resultCode, Uri savedUri) {
                    SnapseedAppDelegate.getInstance().progressEnd();
                    if (resultCode == OperationResult.SUCCESS) {
                        MainActivity.this.invalidateOptionsMenu();
                        SharingHelper.showShareDialog(MainActivity.this, savedUri, shareToGooglePlus);
                    }
                }
            });
            return;
        }
        Uri imageUri = this._editSession.hasChanges() ? this._editSession.getLastSavedImageUri() : this._editSession.getOriginalImageUri();
        if (imageUri == null) {
            imageUri = this._editSession.getLastSavedImageUri();
        }
        SharingHelper.showShareDialog(this, imageUri, shareToGooglePlus);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        int showAction;
        super.onCreateOptionsMenu(menu);
        boolean isTablet = DeviceDefs.isTablet();
        if (isTablet) {
            showAction = 5;
        } else {
            showAction = 2;
        }
        if (!isTablet) {
            this._saveToLibrary = menu.add(0, 17, 0, getString(R.string.save_btn));
            this._saveToLibrary.setIcon(R.drawable.icon_save_default);
            this._saveToLibrary.setShowAsAction(showAction);
            this._shareToGooglePlus = menu.add(0, 15, 0, getString(R.string.google_plus_btn));
            this._shareToGooglePlus.setIcon(R.drawable.icon_sharegplus_default);
            this._shareToGooglePlus.setShowAsAction(showAction);
        }
        this._openItem = menu.add(0, 18, 0, getString(R.string.dialog_open_photo));
        this._openItem.setIcon(R.drawable.icon_openimage_default);
        this._openItem.setShowAsAction(showAction);
        this._shareItem = menu.add(0, 16, 0, getString(R.string.share_btn));
        this._menuImageProperty = menu.add(0, 8, 0, getString(R.string.image));
        this._menuShowOverlay = menu.add(0, 2, 0, getString(R.string.menu_show_overlay));
        this._menuOnlineHelp = menu.add(0, 3, 0, getString(R.string.menu_online_help));
        this._menuAboutSnapseed = menu.add(0, 7, 0, getString(R.string.menu_about));
        return true;
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        updateControlButtonsState();
        return super.onPrepareOptionsMenu(menu);
    }

    private String getHelpBaseUrl() {
        Locale defaultLocale = Locale.getDefault();
        return String.format("https://support.google.com/snapseed/?hl=%s-%s", new Object[]{defaultLocale.getLanguage().toLowerCase(), defaultLocale.getCountry().toLowerCase()});
    }

    public boolean onMenuOpened(int featureId, Menu menu) {
        return super.onMenuOpened(featureId, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (this._isPaused) {
            return $assertionsDisabled;
        }
        super.onOptionsItemSelected(item);
        if (this._rootView != null) {
            this._rootView.hideHelpOverlay($assertionsDisabled);
        }
        Intent intent;
        switch (item.getItemId()) {
            case 2:
                showHelpOverlay();
                break;
            case 3:
                startActivity(new Intent("android.intent.action.VIEW", Uri.parse(getHelpBaseUrl())));
                break;
            case 7:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case 8:
                intent = new Intent(this, ImagePropertyActivity.class);
                intent.setData(this._editSession.getOriginalImageUri());
                Bitmap curImage = this._editSession.getCurrentImage();
                intent.putExtra(ImagePropertyActivity.WIDTH_EXTRA, curImage.getWidth());
                intent.putExtra(ImagePropertyActivity.HEIGHT_EXTRA, curImage.getHeight());
                startActivity(intent);
                break;
            case 12:
                intent = new Intent(this, SystemInfoActivity.class);
                View imageView = this.workingAreaView.getImageView();
                if (imageView instanceof ImageViewGL) {
                    ImageViewGL glImageView = (ImageViewGL) imageView;
                    Enumeration<String> iter = glImageView.getGLProperties().keys();
                    while (iter.hasMoreElements()) {
                        String key = (String) iter.nextElement();
                        intent.putExtra(key, (String) glImageView.getGLProperties().get(key));
                    }
                }
                startActivity(intent);
                break;
            case 15:
                showShareDialog(true);
                break;
            case 16:
                showShareDialog($assertionsDisabled);
                break;
            case 17:
                onSaveImage();
                break;
            case 18:
                showChooseImageSourceDialog();
                break;
        }
        return true;
    }

    private void showChooseImageSourceDialog() {
        final Context context = this;
        askToOpenNewImage(new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int which) {
                if (which != -2) {
                    MainActivity.this.activateMainScreen(true);
                    if (Camera.getNumberOfCameras() > 0) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(R.string.dialog_open_photo);
                        builder.setItems(new String[]{MainActivity.this.getString(R.string.dialog_take_photo), MainActivity.this.getString(R.string.dialog_choose_photo)}, new OnClickListener() {
                            public void onClick(DialogInterface dialogInterface, int itemIndex) {
                                if (itemIndex == 0) {
                                    MainActivity.this.showCapturePhotoDialog();
                                } else {
                                    MainActivity.this.showChoosePhotoDialog();
                                }
                            }
                        });
                        builder.create().show();
                        return;
                    }
                    MainActivity.this.showChoosePhotoDialog();
                }
            }
        });
    }

    public static EditingToolBar getEditingToolbar() {
        return getMainActivity()._rootView.getEditingToolbar();
    }

    public FilterController getFilterController() {
        return this._filterController;
    }

    public void showHelpOverlay() {
        this._rootView.showHelpOverlay(getCurrentScreen() == Screen.MAIN ? R.xml.overlay_main : this._filterController.getHelpResourceId());
    }

    public static WorkingAreaView getWorkingAreaView() {
        return getMainActivity().workingAreaView;
    }

    public static FilterParameter getFilterParameter() {
        return getMainActivity().workingAreaView.getFilterParameter();
    }

    public static MainActivity getMainActivity() {
        return _mainActivity;
    }

    public void onRevertImage() {
        if (this._editSession.hasChanges()) {
            if (this.workingAreaView.isComparing()) {
                this.workingAreaView.endCompare();
            }
            OnClickListener clickListener = new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    MainActivity.this._activeDialog = null;
                    if (which == -1) {
                        MainActivity.this._editSession.revert(MainActivity.this, MainActivity.this);
                    }
                }
            };
            this._activeDialog = new AlertDialog.Builder(this).create();
            this._activeDialog.setTitle(R.string.revert_title);
            this._activeDialog.setButton(-1, getString(R.string.revert_btn), clickListener);
            this._activeDialog.setButton(-2, getString(R.string.cancel_btn), clickListener);
            this._activeDialog.setCanceledOnTouchOutside($assertionsDisabled);
            this._activeDialog.setMessage(getString(R.string.will_revert));
            this._activeDialog.show();
        }
    }

    public void onSaveImage() {
        if (!$assertionsDisabled && this.workingAreaView.getActiveFilterType() != 1000 && this.workingAreaView.getActiveFilterType() != 1) {
            throw new AssertionError("Invalid application state for onSaveImage() call");
        } else if (needsSave()) {
            this._editSession.saveImage(this, this);
        }
    }

    private boolean needsSave() {
        return (this._editSession == null || !(this._editSession.hasUnsavedChanges() || (!this._editSession.hasChanges() && this._editSession.getOriginalImageUri() == null && this._editSession.getLastSavedImageUri() == null))) ? $assertionsDisabled : true;
    }

    public void onExportFilteredImage(Bitmap filteredImage) {
        if (filteredImage != null) {
            this._editSession.applyFilter(getFilterParameter(), filteredImage);
            this.workingAreaView.setImage(this._editSession.getCurrentImage(), this._editSession.getCurrentScreenImage());
        }
        activateMainScreen($assertionsDisabled);
    }

    public OnRenderListener createOnRenderExportListener() {
        return new ExportOnRenderListener();
    }

    private void askToOpenNewImage(OnClickListener listener) {
        if (getCurrentScreen() != Screen.MAIN || this._editSession.hasUnsavedChanges()) {
            AlertDialog dlg = new AlertDialog.Builder(this).create();
            dlg.setTitle(R.string.open_library);
            dlg.setMessage(getString(R.string.open_library_message));
            dlg.setButton(-1, getString(R.string.open_library_btn), listener);
            dlg.setButton(-2, getString(R.string.cancel_btn), listener);
            dlg.show();
            return;
        }
        listener.onClick(null, -1);
    }

    private void showChoosePhotoDialog() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction("android.intent.action.GET_CONTENT");
        startActivityForResult(intent, 1);
    }

    private void showCapturePhotoDialog() {
        if (!this._callsCameraIntent) {
            this._callsCameraIntent = true;
            EditSession editSession = this._editSession;
            this._cameraImageUri = Uri.fromFile(new File(EditSession.getNextSavePath(CAMERA_FILE_NAME)));
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            intent.putExtra("output", this._cameraImageUri);
            intent.putExtra("android.intent.extra.videoQuality", 1);
            startActivityForResult(intent, 2);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (resultCode == -1) {
                MediaScannerConnection.scanFile(this, new String[]{this._cameraImageUri.getPath()}, null, new OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        if (uri != null) {
                            MainActivity.this._cameraImageUri = uri;
                        }
                        MainActivity.this._editSession.loadImage(MainActivity.this, MainActivity.this._cameraImageUri, MainActivity.this);
                    }
                });
            } else {
                if (this._cameraImageUri != null) {
                    new File(this._cameraImageUri.getPath()).delete();
                    this._cameraImageUri = null;
                }
                MediaScannerConnection.scanFile(this, new String[]{Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()}, null, null);
            }
            this._callsCameraIntent = $assertionsDisabled;
        } else if (requestCode == 1 && resultCode == -1) {
            this._editSession.loadImage(this, data.getData(), this);
        }
    }

    public Runnable runOnUiThreadDelayed(Runnable runnable, int delay) {
        Handler handler;
        if (this._rootView != null) {
            handler = this._rootView.getHandler();
        } else {
            handler = null;
        }
        if (handler == null) {
            return null;
        }
        handler.postDelayed(runnable, (long) delay);
        return runnable;
    }

    private void showAlertDialogWithMessage(final int messageId) {
        runOnUiThread(new Runnable() {
            public void run() {
                AlertDialog dlg = new AlertDialog.Builder(MainActivity.this).create();
                dlg.setMessage(MainActivity.this.getResources().getString(messageId));
                dlg.show();
            }
        });
    }

    private void showAlertDialogWithMessage(final String message, final int dismissDelay) {
        runOnUiThread(new Runnable() {
            public void run() {
                final AlertDialog dlg = new AlertDialog.Builder(MainActivity.this).create();
                dlg.setMessage(message);
                dlg.show();
                if (dismissDelay > 0) {
                    MainActivity.this.runOnUiThreadDelayed(new Runnable() {
                        public void run() {
                            dlg.dismiss();
                        }
                    }, dismissDelay);
                }
            }
        });
    }

    public void lockCurrentOrientation() {
        int i = 1;
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (getResources().getConfiguration().orientation) {
            case 1:
                if (VERSION.SDK_INT < 8) {
                    setRequestedOrientation(1);
                    return;
                }
                if (rotation == 1 || rotation == 2) {
                    i = 9;
                }
                setRequestedOrientation(i);
                return;
            case 2:
                if (VERSION.SDK_INT < 8) {
                    setRequestedOrientation(0);
                    return;
                }
                i = (rotation == 0 || rotation == 1) ? 0 : 8;
                setRequestedOrientation(i);
                return;
            default:
                return;
        }
    }

    public void unlockCurrentOrientation() {
        setRequestedOrientation(4);
    }

    private String getTrackerId() {
        return getString(R.string.ga_trackingId);
    }

    public Screen getCurrentScreen() {
        return this.mCurrentScreen;
    }

    public static RootView getRootView() {
        return getMainActivity()._rootView;
    }

    private void setUpGlobalToolBarListeners() {
        GlobalToolBar globalToolBar = this._rootView.getGlobalToolBar();
        if (globalToolBar != null) {
            globalToolBar.getCompareButton().setOnTouchListener(new CompareOnTouchListener());
            globalToolBar.getRevertButton().setOnClickListener(new RevertButtonListener());
            View saveButton = globalToolBar.getSaveButton();
            if (saveButton != null) {
                saveButton.setOnClickListener(new SaveButtonListener());
            }
            globalToolBar.getShareButton().setOnClickListener(new ShareButtonListener());
        }
    }

    public void activateEditScreen(int type) {
        if (!this._editSession.isBusy()) {
            this._newFilterType = type;
            if (this.mCurrentScreen == Screen.MAIN && !this._rootView.isRunningAnimation()) {
                TrackerData.getInstance().sendView(FilterType.getFilterName(type));
                this.mStartTimeOfFilter = System.nanoTime();
                ActionBar actionBar = getActionBar();
                if (actionBar == null) {
                    throw new IllegalStateException("Failed to get action bar reference");
                }
                actionBar.hide();
                activateSSMScreen(Screen.EDIT_CONTROLS_LEFT, true);
                this.workingAreaView.setFilterType(type);
                View helpButton = this.workingAreaView.getHelpButton();
                if (helpButton != null) {
                    helpButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            TrackerData.getInstance().sendView(TrackerData.VIEW_HELP_SCREEN);
                            MainActivity.this.showHelpOverlay();
                        }
                    });
                }
                View compareButton = this.workingAreaView.getCompareButton();
                if (compareButton != null) {
                    compareButton.setOnTouchListener(new CompareOnTouchListener());
                }
            }
        }
    }

    public void activateMainScreen(boolean back) {
        if (this.mCurrentScreen != Screen.MAIN && !this._rootView.isRunningAnimation()) {
            TrackerData.getInstance().sendView(TrackerData.VIEW_MAIN_SCREEN);
            long deltaInSeconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - this.mStartTimeOfFilter);
            this.mStartTimeOfFilter = 0;
            String filterName = FilterType.getFilterName(this._newFilterType);
            if (back) {
                TrackerData.getInstance().cancelFilter(filterName, deltaInSeconds);
            } else {
                TrackerData.getInstance().addFilter(filterName, deltaInSeconds);
            }
            getEditingToolbar().setSystemUiVisibility(0);
            this._newFilterType = 1;
            if (!back) {
                NotificationCenter.getInstance().performAction(ListenerType.DidApplyFilter, null);
            }
            activateSSMScreen(back ? Screen.EDIT_CONTROLS_LEFT : Screen.EDIT_CONTROLS_RIGHT, $assertionsDisabled);
            this.workingAreaView.setFilterType(1);
            this.workingAreaView.updateImageViewType(1);
            activateSSMScreen(Screen.MAIN, true);
            this._rootView.post(new Runnable() {
                public void run() {
                    ActionBar actionBar = MainActivity.this.getActionBar();
                    if (actionBar != null) {
                        actionBar.show();
                    }
                }
            });
        }
    }

    private void activateSSMScreen(Screen screen, boolean animate) {
        this._rootView.hideHelpOverlay($assertionsDisabled);
        ArrayList<Animator> animations = this._rootView.willEnterSMScreen(screen, animate);
        this.mCurrentScreen = screen;
        this._rootView.didEnterSMScreen(screen, animate, animations);
        if (screen != Screen.MAIN) {
            EditingToolBar editingToolBar = getEditingToolbar();
            if (editingToolBar == null) {
                throw new IllegalStateException("Editing toolbar cannot be null at this point");
            }
            View compareButton = editingToolBar.getCompareButton();
            if (compareButton != null) {
                compareButton.setOnTouchListener(new CompareOnTouchListener());
            }
        } else {
            invalidateOptionsMenu();
        }
        setUpGlobalToolBarListeners();
    }

    public int getMaxScreenEdgeSize() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        Resources resources = getResources();
        int border = resources.getDimensionPixelSize(R.dimen.wa_border_width);
        return Math.max(metrics.widthPixels - (border * 2), (metrics.heightPixels - (border * 2)) - ((int) resources.getDimension(R.dimen.tmp_editing_toolbar_height)));
    }

    private int getSampleImageResId() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        return Math.min(metrics.widthPixels, metrics.heightPixels) > THRESHOLD_SMALLER_WIDTH ? R.drawable.start_hd : R.drawable.start;
    }

    private void switchMainScreenCompareMode(boolean compareOn) {
        if (this.workingAreaView.isComparing() != compareOn) {
            if (compareOn) {
                this.workingAreaView.beginCompare(this._editSession.getOriginalScreenImage());
            } else {
                this.workingAreaView.endCompare();
            }
        }
    }

    private void switchEditScreenCompareMode(int filterType, boolean compareOn) {
        if (filterType == 6) {
            ((CropController) this._filterController).setPreviewVisible(compareOn);
            getEditingToolbar().setPreviewShown(compareOn);
        } else if (filterType == 5) {
            ((StraightenController) this._filterController).setPreviewVisible(compareOn);
            getEditingToolbar().setPreviewShown(compareOn);
        } else {
            NativeCore.getInstance().setCompare(compareOn);
            this.workingAreaView.requestRender();
            NotificationCenter.getInstance().performAction(ListenerType.DidChangeCompareImageMode, Boolean.valueOf(compareOn));
        }
    }

    public OnPreviewRenderListener getOnFirstFrameListener() {
        return new OnPreviewRenderListener() {
            public void onPreviewRendered() {
                MainActivity.this.workingAreaView.activateGLImageViewAnimated();
                MainActivity.this.runOnUiThreadDelayed(new Runnable() {
                    public void run() {
                        EditingToolBar editingToolbar = MainActivity.getEditingToolbar();
                        if (editingToolbar != null) {
                            editingToolbar.setEnabled(true);
                            FilterController controller = MainActivity.this.getFilterController();
                            if (controller != null) {
                                controller.updateEditingToolbarState();
                            }
                        }
                    }
                }, RootView.ANIMATION_TIME);
            }
        };
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(Settings.ENABLE_GOOGLE_ANALYTICS_KEY)) {
            TrackerData.getInstance().updateTracker(createTracker());
        }
    }

    private static int getDalvikHeapSize() {
        int heapSize = 0;
        try {
            Process process = Runtime.getRuntime().exec(GETPROP_HEAPSIZE_COMMAND);
            String heapSizeString = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
            process.destroy();
            heapSize = parseHeapSizeString(heapSizeString);
        } catch (IOException e) {
        }
        return heapSize;
    }

    private static int parseHeapSizeString(String heapSizeString) {
        int i = 0;
        if (Pattern.compile("[0-9]+[m|M]").matcher(heapSizeString).matches()) {
            try {
                i = Integer.parseInt(heapSizeString.substring(0, heapSizeString.length() - 1));
            } catch (NumberFormatException e) {
            }
        }
        return i;
    }

    private void updateControlButtonsState() {
        GlobalToolBar globalToolBar = this._rootView.getGlobalToolBar();
        if (globalToolBar != null) {
            boolean hasChanges = this._editSession.hasChanges();
            View compareButton = globalToolBar.getCompareButton();
            if (compareButton != null) {
                compareButton.setEnabled(hasChanges);
            }
            globalToolBar.getRevertButton().setEnabled(hasChanges);
            View saveButton = globalToolBar.getSaveButton();
            if (saveButton != null) {
                saveButton.setEnabled(this._editSession.hasUnsavedChanges());
            }
            boolean canShare = (!this._editSession.isUsingSampleImage() || hasChanges) ? true : $assertionsDisabled;
            View shareButton = globalToolBar.getShareButton();
            if (shareButton != null) {
                shareButton.setEnabled(canShare);
            }
            setMenuItemEnabled(this._saveToLibrary, this._editSession.hasUnsavedChanges());
            setMenuItemEnabled(this._shareToGooglePlus, canShare);
            setMenuItemEnabled(this._shareItem, canShare);
        }
    }

    private void setMenuItemEnabled(MenuItem menuItem, boolean enabled) {
        if (menuItem != null) {
            menuItem.setEnabled(enabled);
            Drawable menuItemIcon = menuItem.getIcon();
            if (menuItemIcon != null) {
                menuItemIcon.setAlpha(enabled ? ALPHA_OPAQUE : ALPHA_30_PERCENT_TRANSPARENT);
            }
        }
    }
}

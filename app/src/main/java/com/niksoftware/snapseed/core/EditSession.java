package com.niksoftware.snapseed.core;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import com.niksoftware.snapseed.core.EditSessionInterface.LoadImageListener;
import com.niksoftware.snapseed.core.EditSessionInterface.OperationResult;
import com.niksoftware.snapseed.core.EditSessionInterface.RemoveFilterListener;
import com.niksoftware.snapseed.core.EditSessionInterface.RevertImageListener;
import com.niksoftware.snapseed.core.EditSessionInterface.SaveImageListener;
import com.niksoftware.snapseed.core.filterparameters.FilterParameter;
import com.niksoftware.snapseed.util.ExifData;
import com.niksoftware.snapseed.util.FileHelper;
import com.niksoftware.snapseed.util.MetaData;
import com.niksoftware.snapseed.util.XmlHelper;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class EditSession {
    static final /* synthetic */ boolean $assertionsDisabled = (!EditSession.class.desiredAssertionStatus() ? true : false);
    private static final String BACKUP_BASE_FILENAME = "snapseed.jpeg";
    private static final String CURRENT_FILEPATH_KEY = "edit.currentImagePath";
    private static final String DEFAULT_IMAGE_FILENAME = "Snapseed";
    private static final String EXIF_SOFTWARE_ID_STRING = "Snapseed 1.6";
    private static final int FILE_NAME_RANDOM_MAX_RETRY_COUNT = 16;
    private static final int FILE_NAME_RANDOM_SUFIX_MAX = 10000;
    private static final String FILTER_CHAIN_KEY = "edit.filterChain";
    private static final String HAS_UNSAVED_CHANGES_KEY = "edit.hasUnsavedChanges";
    private static final int IMAGE_BACKUP_JPEG_QUALITY = 100;
    private static final String IMAGE_DIRECTORY_NAME = "Snapseed";
    private static final int IMAGE_SAVE_JPEG_QUALITY = 95;
    private static final String LOG_TAG = "snapseed";
    private static final String ORIGINAL_BACKUP_FILEPATH_KEY = "edit.originalImageBackupPath";
    private static final String ORIGINAL_FILENAME_KEY = "edit.originalFileName";
    private static final String ORIGINAL_FILEPATH_KEY = "edit.originalImagePath";
    private static final String SCREEN_FILEPATH_KEY = "edit.screenImagePath";
    private final ContentResolver _contentResolver;
    private Bitmap _currentImage;
    private String _currentImageBackupPath;
    private Bitmap _currentScreenImage;
    private FilterChain _filterChain = new FilterChain();
    private boolean _hasUnsavedChanges = $assertionsDisabled;
    private boolean _isBusy;
    private Uri _lastSavedImageUri;
    private MetaData _metadata;
    private String _originalFileName;
    private Bitmap _originalImage;
    private String _originalImageBackupPath;
    private Uri _originalImageUri;
    private Bitmap _originalScreenImage;
    private final Handler _uiThreadHandler;
    private final int defaultImageResourceId;
    private final int maxImagePixelCount;
    private final int maxScreenImageEdgeSize;
    private final int minImageEdgeSize;
    private int originalImageHeight;
    private int originalImageWidth;

    enum BackupFileType {
        OriginalImage,
        CurrentImage,
        ScreenImage
    }

    private class ImageLoadData {
        public Context context;
        public int decodedHeight;
        public int decodedWidth;
        public LoadImageListener loadImageListener;
        public boolean loadMetadata;
        public String mimeType;
        public boolean resizedInputImage;
        public OperationResult result;
        public Uri sourceUri;

        private ImageLoadData() {
        }
    }

    public EditSession(int defaultImageResourceId, int maxScreenImageEdgeSize, int minImageEdgeSize, int maxImagePixelCount, Handler uiThreadHandler, ContentResolver contentResolver) {
        this.defaultImageResourceId = defaultImageResourceId;
        this.maxScreenImageEdgeSize = maxScreenImageEdgeSize;
        this.minImageEdgeSize = minImageEdgeSize;
        this.maxImagePixelCount = maxImagePixelCount;
        this._uiThreadHandler = uiThreadHandler;
        this._contentResolver = contentResolver;
    }

    public synchronized void cleanup() {
        this._originalImageUri = null;
        this._originalImageBackupPath = null;
        this._currentImageBackupPath = null;
        this._originalFileName = null;
        if (this._originalImage != null) {
            this._originalImage.recycle();
            this._originalImage = null;
        }
        if (this._currentImage != null) {
            this._currentImage.recycle();
            this._currentImage = null;
        }
        this._currentScreenImage = null;
        this._originalScreenImage = null;
        this._metadata = null;
        this._lastSavedImageUri = null;
        this._hasUnsavedChanges = $assertionsDisabled;
        this._isBusy = $assertionsDisabled;
        this._filterChain.clear();
    }

    public boolean loadImage(final Context context, final Uri imageUri, final LoadImageListener loadImageListener) {
        if (this._isBusy) {
            return $assertionsDisabled;
        }
        if (loadImageListener == null) {
            return loadImageSync(context, imageUri, null, $assertionsDisabled);
        }
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                EditSession.this.loadImageSync(context, imageUri, loadImageListener, EditSession.$assertionsDisabled);
            }
        }).start();
        return true;
    }

    public boolean saveImage(final Context context, final SaveImageListener saveImageListener) {
        if (this._isBusy) {
            return $assertionsDisabled;
        }
        if (saveImageListener == null) {
            return saveImageSync(context, null);
        }
        new Thread(new Runnable() {
            public void run() {
                EditSession.this.saveImageSync(context, saveImageListener);
            }
        }).start();
        return true;
    }

    public boolean revert(final Context context, final RevertImageListener revertImageListener) {
        if (this._isBusy) {
            return $assertionsDisabled;
        }
        if (revertImageListener == null) {
            return revertSync(context, null);
        }
        new Thread(new Runnable() {
            public void run() {
                EditSession.this.revertSync(context, revertImageListener);
            }
        }).start();
        return true;
    }

    public boolean removeLastFilter(final Context context, final RemoveFilterListener listener) {
        if (this._isBusy) {
            return $assertionsDisabled;
        }
        new Thread(new Runnable() {
             final /* synthetic */ boolean $assertionsDisabled = (!EditSession.class.desiredAssertionStatus() ? true : false);

            public void run() {
                if (listener != null) {
                    EditSession.this._uiThreadHandler.post(new Runnable() {
                        public void run() {
                            listener.onRemoveFilterStarted(EditSession.this);
                        }
                    });
                }
                Bitmap originalImage = EditSession.this.getOriginalImage();
                if (originalImage == null) {
                    originalImage = BitmapFactory.decodeResource(context.getResources(), EditSession.this.defaultImageResourceId, EditSession.getDefaultLoadOptions());
                }
                if ($assertionsDisabled || originalImage != EditSession.this._originalImage) {
                    if (originalImage.getConfig() != Config.ARGB_8888) {
                        Bitmap orig = originalImage.copy(Config.ARGB_8888, true);
                        originalImage.recycle();
                        originalImage = orig;
                    }
                    if ($assertionsDisabled || EditSession.this._filterChain.size() > 1) {
                        EditSession.this._filterChain.removeLastFilterNode();
                        Bitmap bitmap = NativeCore.renderFilterChain(originalImage, EditSession.this._filterChain);
                        final OperationResult result = bitmap == null ? OperationResult.ERROR_REMOVE_LAST_FILTER_FAILED : OperationResult.SUCCESS;
                        if (bitmap != null) {
                            EditSession.this._currentImage = bitmap;
                            EditSession.this._currentScreenImage = null;
                            EditSession.this._currentScreenImage = EditSession.this.createScreenImage(EditSession.this._currentImage);
                        }
                        if (listener != null) {
                            EditSession.this._uiThreadHandler.post(new Runnable() {
                                public void run() {
                                    listener.onRemoveFilterFinished(EditSession.this, result);
                                }
                            });
                            return;
                        }
                        return;
                    }
                    throw new AssertionError();
                }
                throw new AssertionError();
            }
        }).start();
        return true;
    }

    public Bitmap getOriginalImage() {
        if (this._originalImage != null) {
            return this._originalImage;
        }
        Bitmap originalImage = null;
        if (this._originalImageBackupPath != null) {
            originalImage = loadBitmap(this._originalImageBackupPath, null);
        }
        if (originalImage != null || this._originalImageUri == null) {
            return originalImage;
        }
        return loadBitmap(this._originalImageUri, null);
    }

    public Bitmap getCurrentImage() {
        Bitmap currentImage = getActualCurrentImage();
        return currentImage == null ? getOriginalImage() : currentImage;
    }

    private Bitmap getActualCurrentImage() {
        if (this._currentImage != null) {
            return this._currentImage;
        }
        if (this._currentImageBackupPath != null) {
            this._currentImage = loadBitmap(this._currentImageBackupPath, null);
        }
        return this._currentImage;
    }

    public Bitmap getOriginalScreenImage() {
        if (this._originalScreenImage == null) {
            this._originalScreenImage = createScreenImage(getOriginalImage());
        }
        return this._originalScreenImage;
    }

    public Bitmap getCurrentScreenImage() {
        if (!hasChanges()) {
            return getOriginalScreenImage();
        }
        if (this._currentScreenImage == null) {
            this._currentScreenImage = createScreenImage(getCurrentImage());
        }
        return this._currentScreenImage;
    }

    public int getOriginalImageWidth() {
        return this.originalImageWidth;
    }

    public int getOriginalImageHeight() {
        return this.originalImageHeight;
    }

    public synchronized void applyFilter(FilterParameter filter, Bitmap filteredImage) {
        if (filteredImage == null) {
            throw new IllegalArgumentException("Invalid input (filteredImage cannot be null)");
        }
        if (filter != null) {
            this._filterChain.addFilterParameter(filter);
        }
        this._originalImage = null;
        if (filteredImage != this._currentImage) {
            if (this._currentImage != null) {
                this._currentImage.recycle();
            }
            this._currentImage = filteredImage;
        }
        this._currentScreenImage = null;
        getCurrentScreenImage();
        this._hasUnsavedChanges = true;
        if (filter != null && filter.affectsPanorama()) {
            trimPhotoSphereData();
        }
    }

    public boolean isUsingSampleImage() {
        return this._originalImageUri == null ? true : $assertionsDisabled;
    }

    public boolean hasChanges() {
        return (this._currentImage == null && this._currentImageBackupPath == null) ? $assertionsDisabled : true;
    }

    public boolean hasUnsavedChanges() {
        return this._hasUnsavedChanges;
    }

    public Uri getOriginalImageUri() {
        return this._originalImageUri;
    }

    public Uri getLastSavedImageUri() {
        return this._lastSavedImageUri;
    }

    public FilterChain getFilterChainNew() {
        return this._filterChain;
    }

    public boolean saveState(Context context, Bundle outState) {
        String str = null;
        if (this._originalImageUri == null && this._currentImage == null) {
            return true;
        }
        boolean succeeded = true;
        String curBackupPath = null;
        if (this._currentImage != null) {
            File curBackupFile = getBackupFile(context, BackupFileType.CurrentImage);
            if (curBackupFile != null) {
                curBackupPath = curBackupFile.getPath();
                succeeded = saveBitmap(this._currentImage, curBackupPath, 100, this._metadata);
            } else {
                succeeded = $assertionsDisabled;
            }
        }
        String screenBackupPath = null;
        Bitmap screenImage = getCurrentScreenImage();
        if (succeeded && screenImage != null) {
            File screenBackupFile = getBackupFile(context, BackupFileType.ScreenImage);
            if (screenBackupFile != null) {
                screenBackupPath = screenBackupFile.getPath();
                succeeded = saveBitmap(screenImage, screenBackupPath, 100, null);
            } else {
                succeeded = $assertionsDisabled;
            }
        }
        if (!succeeded) {
            return succeeded;
        }
        String str2 = ORIGINAL_FILEPATH_KEY;
        if (this._originalImageUri != null) {
            str = this._originalImageUri.toString();
        }
        outState.putString(str2, str);
        outState.putString(ORIGINAL_BACKUP_FILEPATH_KEY, this._originalImageBackupPath);
        outState.putString(ORIGINAL_FILENAME_KEY, this._originalFileName);
        outState.putString(CURRENT_FILEPATH_KEY, curBackupPath);
        outState.putString(SCREEN_FILEPATH_KEY, screenBackupPath);
        outState.putBoolean(HAS_UNSAVED_CHANGES_KEY, this._hasUnsavedChanges);
        if (this._filterChain.size() <= 0) {
            return succeeded;
        }
        outState.putString(FILTER_CHAIN_KEY, this._filterChain.toStringJson());
        return succeeded;
    }

    public boolean restoreState(Context context, Bundle inState) {
        if (inState == null || (!inState.containsKey(ORIGINAL_FILEPATH_KEY) && !inState.containsKey(CURRENT_FILEPATH_KEY))) {
            return $assertionsDisabled;
        }
        Uri parse;
        cleanup();
        String originalPath = inState.getString(ORIGINAL_FILEPATH_KEY);
        if (originalPath != null) {
            parse = Uri.parse(originalPath);
        } else {
            parse = null;
        }
        this._originalImageUri = parse;
        this._originalFileName = inState.getString(ORIGINAL_FILENAME_KEY);
        this._originalImageBackupPath = inState.getString(ORIGINAL_BACKUP_FILEPATH_KEY);
        this._currentImageBackupPath = inState.getString(CURRENT_FILEPATH_KEY);
        this._hasUnsavedChanges = inState.getBoolean(HAS_UNSAVED_CHANGES_KEY);
        String screenImagePath = inState.getString(SCREEN_FILEPATH_KEY);
        if ($assertionsDisabled || screenImagePath != null) {
            if (this._currentImageBackupPath != null) {
                this._currentScreenImage = loadBitmap(screenImagePath, null);
                this._metadata = MetaData.load(this._currentImageBackupPath);
            } else {
                this._originalScreenImage = loadBitmap(screenImagePath, null);
                this._metadata = MetaData.load(this._originalImageBackupPath);
            }
            if (this._metadata == null && this._originalImageUri != null) {
                if ($assertionsDisabled) {
                    this._metadata = MetaData.load(context.getContentResolver(), this._originalImageUri);
                } else {
                    throw new AssertionError("Reading metadata from backup file failed");
                }
            }
            String filterChainString = inState.getString(FILTER_CHAIN_KEY);
            if (filterChainString != null) {
                this._filterChain = FilterChain.parseFilterChainString(filterChainString);
                if (this._filterChain == null) {
                    this._filterChain = new FilterChain();
                }
            }
            return true;
        }
        throw new AssertionError("Screen image path must persist here");
    }

    public boolean isBusy() {
        return this._isBusy;
    }

    private synchronized boolean loadImageSync(Context context, Uri imageUri, final LoadImageListener loadImageListener, boolean forceNoCleanup) {
        boolean succeeded = true;
        synchronized (this) {
            this._isBusy = true;
            if (loadImageListener != null) {
                this._uiThreadHandler.post(new Runnable() {
                    public void run() {
                        loadImageListener.onLoadStarted(EditSession.this);
                    }
                });
            }
            final ImageLoadData imageLoadData = new ImageLoadData();
            imageLoadData.context = context;
            imageLoadData.sourceUri = imageUri;
            imageLoadData.loadImageListener = loadImageListener;
            imageLoadData.loadMetadata = true;
            if (!forceNoCleanup) {
                cleanup();
            }
            if (imageUri != null) {
                inspectImage(imageLoadData);
                if (imageLoadData.result == OperationResult.SUCCESS) {
                    loadImage(imageLoadData);
                }
                if (imageLoadData.result != OperationResult.SUCCESS) {
                    loadSampleImage(imageLoadData);
                }
            } else {
                loadSampleImage(imageLoadData);
                imageLoadData.result = OperationResult.SUCCESS;
            }
            if (this._originalImageUri != null) {
                this._originalFileName = FileHelper.getFileDisplayName(context, this._originalImageUri);
            }
            if (this._originalFileName == null) {
                this._originalFileName = "Snapseed";
            }
            getOriginalScreenImage();
            if (loadImageListener != null) {
                this._uiThreadHandler.post(new Runnable() {
                    public void run() {
                        loadImageListener.onLoadFinished(EditSession.this, imageLoadData.result, imageLoadData.decodedWidth, imageLoadData.decodedHeight);
                    }
                });
            }
            if (imageLoadData.result != OperationResult.SUCCESS) {
                succeeded = $assertionsDisabled;
            }
            if (succeeded && imageUri != null) {
                this._originalImageBackupPath = null;
                MetaData metadata = getFinalImageMetadata(this._metadata, this._originalImage.getWidth(), this._originalImage.getHeight());
                this._originalImageBackupPath = getBackupFile(context, BackupFileType.OriginalImage).getPath();
                succeeded = saveBitmap(this._originalImage, this._originalImageBackupPath, 100, metadata);
            }
            this._isBusy = $assertionsDisabled;
        }
        return succeeded;
    }

    private synchronized boolean revertSync(Context context, final RevertImageListener revertImageListener) {
        boolean z = true;
        synchronized (this) {
            this._isBusy = true;
            this._filterChain.clear();
            OperationResult result = OperationResult.SUCCESS;
            if (revertImageListener != null) {
                this._uiThreadHandler.post(new Runnable() {
                    public void run() {
                        revertImageListener.onRevertStarted(EditSession.this);
                    }
                });
            }
            if (this._currentImage != null) {
                this._currentImage = null;
                this._currentScreenImage = null;
                this._originalImage = null;
                if (this._originalImageUri != null) {
                    Bitmap restoredImage;
                    if (this._originalImageBackupPath != null) {
                        restoredImage = loadBitmap(this._originalImageBackupPath, null);
                    } else {
                        restoredImage = loadBitmap(this._originalImageUri, null);
                    }
                    setOriginalImageSafe(restoredImage);
                    if (this._originalImage == null) {
                        result = OperationResult.ERROR_COULD_NOT_DECODE;
                    }
                }
                if (this._originalImage == null) {
                    ImageLoadData imageLoadData = new ImageLoadData();
                    imageLoadData.context = context;
                    loadSampleImage(imageLoadData);
                }
                this._originalScreenImage = null;
                this._hasUnsavedChanges = $assertionsDisabled;
            } else {
                result = OperationResult.WARNING_NOTHING_TO_REVERT;
            }
            this._isBusy = $assertionsDisabled;
            if (revertImageListener != null) {
                final OperationResult resultCopy = result;
                this._uiThreadHandler.post(new Runnable() {
                    public void run() {
                        revertImageListener.onRevertFinished(EditSession.this, resultCopy);
                    }
                });
            }
            if (result != OperationResult.SUCCESS) {
                z = $assertionsDisabled;
            }
        }
        return z;
    }

    private synchronized boolean saveImageSync(Context context, final SaveImageListener saveImageListener) {
        boolean z = true;
        synchronized (this) {
            OperationResult result;
            this._isBusy = true;
            if (saveImageListener != null) {
                this._uiThreadHandler.post(new Runnable() {
                    public void run() {
                        saveImageListener.onSaveStarted(EditSession.this);
                    }
                });
            }
            if (FileHelper.isSDCardWritable()) {
                String newImagePath = getNextSavePath(this._originalFileName);
                if ($assertionsDisabled || newImagePath != null) {
                    Bitmap currentImage = getCurrentImage();
                    if (saveBitmap(currentImage, newImagePath, IMAGE_SAVE_JPEG_QUALITY, getFinalImageMetadata(this._metadata, currentImage.getWidth(), currentImage.getHeight()))) {
                        this._lastSavedImageUri = FileHelper.scanMediaFile(context, newImagePath);
                        result = OperationResult.SUCCESS;
                    } else {
                        result = OperationResult.ERROR_SAVE_FAILED;
                    }
                } else {
                    throw new AssertionError("Save path generation routine failed!");
                }
            }
            result = OperationResult.ERROR_SAVE_SD_NOT_WRITABLE;
            this._hasUnsavedChanges = result != OperationResult.SUCCESS ? true : $assertionsDisabled;
            this._isBusy = $assertionsDisabled;
            if (saveImageListener != null) {
                final OperationResult resultCopy = result;
                this._uiThreadHandler.post(new Runnable() {
                    public void run() {
                        saveImageListener.onSaveFinished(EditSession.this, resultCopy, EditSession.this._lastSavedImageUri);
                    }
                });
            }
            if (result != OperationResult.SUCCESS) {
                z = $assertionsDisabled;
            }
        }
        return z;
    }

    private void inspectImage(ImageLoadData imageLoadData) {
        imageLoadData.result = OperationResult.SUCCESS;
        imageLoadData.decodedWidth = 0;
        imageLoadData.decodedHeight = 0;
        try {
            Options options = new Options();
            InputStream imageStream = this._contentResolver.openInputStream(imageLoadData.sourceUri);
            if (imageStream != null) {
                options.inPreferredConfig = Config.ARGB_8888;
                options.inScaled = $assertionsDisabled;
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(imageStream, null, options);
                imageStream.close();
            }
            if (options.outWidth <= 0 || options.outHeight <= 0) {
                throw new Exception();
            }
            float zoomFactor = Math.max(((float) options.outWidth) / ((float) this.maxScreenImageEdgeSize), ((float) options.outHeight) / ((float) this.maxScreenImageEdgeSize));
            if (((float) options.outWidth) / zoomFactor < ((float) this.minImageEdgeSize) || ((float) options.outHeight) / zoomFactor < ((float) this.minImageEdgeSize)) {
                imageLoadData.result = OperationResult.ERROR_EXTREME_ASPECT_RATIO;
                return;
            }
            imageLoadData.decodedWidth = options.outWidth;
            imageLoadData.decodedHeight = options.outHeight;
            imageLoadData.mimeType = options.outMimeType;
        } catch (FileNotFoundException e) {
            imageLoadData.result = OperationResult.ERROR_COULD_NOT_OPEN;
        } catch (Exception e2) {
            imageLoadData.result = OperationResult.ERROR_COULD_NOT_DECODE;
        }
    }

    private void loadImage(ImageLoadData imageLoadData) {
        Options bitmapLoadOptions = getDefaultLoadOptions();
        this.originalImageWidth = 0;
        this.originalImageHeight = 0;
        imageLoadData.resizedInputImage = $assertionsDisabled;
        int width = imageLoadData.decodedWidth;
        int height = imageLoadData.decodedHeight;
        if (width * height > 0) {
            int inSampleSize = 1;
            while (width * height > this.maxImagePixelCount) {
                inSampleSize *= 2;
                width = ((imageLoadData.decodedWidth + inSampleSize) - 1) / inSampleSize;
                height = ((imageLoadData.decodedHeight + inSampleSize) - 1) / inSampleSize;
            }
            if (inSampleSize > 1) {
                bitmapLoadOptions.inSampleSize = inSampleSize;
                bitmapLoadOptions.inDither = true;
                bitmapLoadOptions.outWidth = width;
                bitmapLoadOptions.outHeight = height;
                imageLoadData.resizedInputImage = true;
            }
        }
        if (imageLoadData.sourceUri != null) {
            Bitmap sourceImage = loadBitmap(imageLoadData.sourceUri, bitmapLoadOptions);
            if (sourceImage == null) {
                imageLoadData.result = OperationResult.ERROR_COULD_NOT_DECODE;
                return;
            }
            this.originalImageWidth = width;
            this.originalImageHeight = height;
            this._metadata = null;
            if (imageLoadData.loadMetadata) {
                this._metadata = MetaData.load(FileHelper.getFilePath(imageLoadData.context, imageLoadData.sourceUri));
                if (this._metadata == null) {
                    this._metadata = MetaData.load(imageLoadData.context.getContentResolver(), imageLoadData.sourceUri);
                }
            }
            int initialImageWidth = sourceImage.getWidth();
            int initialImageHeight = sourceImage.getHeight();
            if (initialImageWidth < this.minImageEdgeSize || initialImageHeight < this.minImageEdgeSize) {
                float scale = Math.max(((float) this.minImageEdgeSize) / ((float) initialImageWidth), ((float) this.minImageEdgeSize) / ((float) initialImageHeight));
                sourceImage = Bitmap.createScaledBitmap(sourceImage, Math.round(((float) initialImageWidth) * scale), Math.round(((float) initialImageHeight) * scale), true);
            }
            if (this._metadata != null) {
                Integer orientation = this._metadata.getExifInteger(ExifData.TAG_ORIENTATION);
                Matrix matrix = ExifData.getTransformationMatrix(orientation != null ? orientation.intValue() : 0, initialImageWidth, initialImageHeight);
                if (!matrix.isIdentity()) {
                    Bitmap rotatedImage = null;
                    try {
                        rotatedImage = Bitmap.createBitmap(sourceImage, 0, 0, initialImageWidth, initialImageHeight, matrix, $assertionsDisabled);
                    } catch (Exception e) {
                    }
                    if (rotatedImage != null) {
                        if (rotatedImage != sourceImage) {
                            sourceImage.recycle();
                        }
                        sourceImage = rotatedImage;
                        this._metadata.setExifShort(ExifData.TAG_ORIENTATION, Integer.valueOf(1));
                    }
                }
            }
            this._originalImageUri = imageLoadData.sourceUri;
            setOriginalImageSafe(sourceImage);
        } else {
            loadSampleImage(imageLoadData);
        }
        imageLoadData.result = OperationResult.SUCCESS;
    }

    private void loadSampleImage(ImageLoadData imageLoadData) {
        Options bitmapLoadOptions = getDefaultLoadOptions();
        this._originalImageUri = null;
        this._originalImage = BitmapFactory.decodeResource(imageLoadData.context.getResources(), this.defaultImageResourceId, bitmapLoadOptions);
        this._originalScreenImage = null;
        this._originalFileName = "Snapseed";
        this.originalImageWidth = this._originalImage.getWidth();
        this.originalImageHeight = this._originalImage.getHeight();
    }

    public static String getNextSavePath(String fileName) {
        File outputFolder = new File(Environment.getExternalStorageDirectory(), "Snapseed");
        if (!outputFolder.exists() && !outputFolder.mkdirs()) {
            return null;
        }
        File imageFile;
        int counter = 0;
        do {
            Object[] objArr = new Object[2];
            objArr[0] = fileName;
            counter++;
            objArr[1] = Integer.valueOf(counter);
            imageFile = new File(outputFolder, String.format("%s_%d.jpg", objArr));
        } while (imageFile.exists());
        return imageFile.getPath();
    }

    private MetaData getFinalImageMetadata(MetaData sourceMetadata, int imageWidth, int imageHeight) {
        MetaData metadata;
        if (sourceMetadata != null) {
            sourceMetadata.setExifInteger(ExifData.TAG_EXIF_IMAGE_WIDTH, Integer.valueOf(imageWidth));
            sourceMetadata.setExifInteger(ExifData.TAG_EXIF_IMAGE_HEIGHT, Integer.valueOf(imageHeight));
            Integer orientation = sourceMetadata.getExifInteger(ExifData.TAG_ORIENTATION);
            sourceMetadata.updateXmpSection(imageWidth, imageHeight, orientation != null ? orientation.intValue() : 1);
            metadata = sourceMetadata;
        } else {
            metadata = MetaData.create(new Date(), imageWidth, imageHeight);
        }
        metadata.setExifDate(ExifData.TAG_DATE_TIME, new Date());
        metadata.setExifString(ExifData.TAG_SOFTWARE, EXIF_SOFTWARE_ID_STRING);
        return metadata;
    }

    private File getBackupFile(Context context, BackupFileType cacheFileType) {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir == null || ((!cacheDir.exists() && !cacheDir.mkdirs()) || !cacheDir.canWrite())) {
            return null;
        }
        String cacheFileName;
        switch (cacheFileType) {
            case OriginalImage:
                cacheFileName = "org_snapseed.jpeg";
                break;
            case CurrentImage:
                cacheFileName = "cur_snapseed.jpeg";
                break;
            case ScreenImage:
                cacheFileName = "scr_snapseed.jpeg";
                break;
            default:
                cacheFileName = null;
                break;
        }
        return new File(cacheDir, cacheFileName);
    }

    private Bitmap loadBitmap(Uri imageUri, Options bitmapOptions) {
        if (bitmapOptions == null) {
            bitmapOptions = getDefaultLoadOptions();
        }
        Bitmap result = null;
        try {
            InputStream imageStream = this._contentResolver.openInputStream(imageUri);
            result = BitmapFactory.decodeStream(imageStream, null, bitmapOptions);
            imageStream.close();
            return result;
        } catch (Exception e) {
            return result;
        }
    }

    private Bitmap loadBitmap(String imagePath, Options bitmapOptions) {
        if (bitmapOptions == null) {
            bitmapOptions = getDefaultLoadOptions();
        }
        Bitmap result = null;
        try {
            InputStream imageStream = new FileInputStream(imagePath);
            result = BitmapFactory.decodeStream(imageStream, null, bitmapOptions);
            imageStream.close();
            return result;
        } catch (Exception e) {
            return result;
        }
    }

    private boolean saveBitmap(Bitmap bitmap, String bitmapPath, int quality, MetaData metaData) {
        boolean result = $assertionsDisabled;
        try {
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(bitmapPath, $assertionsDisabled));
            result = quality < 0 ? bitmap.compress(CompressFormat.PNG, 100, outputStream) : bitmap.compress(CompressFormat.JPEG, quality, outputStream);
            outputStream.close();
            if (metaData != null) {
                metaData.save(bitmapPath);
            }
        } catch (Exception e) {
        }
        return result;
    }

    private Bitmap createScreenImage(Bitmap source) {
        if (source == null) {
            return null;
        }
        if (source.getConfig() != Config.ARGB_8888) {
            throw new IllegalArgumentException("Bad image pixel format");
        }
        float maxScale = Math.max(((float) source.getWidth()) / ((float) this.maxScreenImageEdgeSize), ((float) source.getHeight()) / ((float) this.maxScreenImageEdgeSize));
        return maxScale <= 1.0f ? source.copy(Config.ARGB_8888, $assertionsDisabled) : Bitmap.createScaledBitmap(source, Math.round(((float) source.getWidth()) / maxScale), Math.round(((float) source.getHeight()) / maxScale), true);
    }

    private static Options getDefaultLoadOptions() {
        Options defaultLoadOptions = new Options();
        defaultLoadOptions.inPreferredConfig = Config.ARGB_8888;
        defaultLoadOptions.inDither = $assertionsDisabled;
        defaultLoadOptions.inScaled = $assertionsDisabled;
        defaultLoadOptions.inPreferQualityOverSpeed = true;
        defaultLoadOptions.inMutable = $assertionsDisabled;
        defaultLoadOptions.inPurgeable = true;
        return defaultLoadOptions;
    }

    private void setOriginalImageSafe(Bitmap newImage) {
        if (newImage == null || newImage.getConfig() == Config.ARGB_8888) {
            this._originalImage = newImage;
            return;
        }
        this._originalImage = newImage.copy(Config.ARGB_8888, $assertionsDisabled);
        newImage.recycle();
    }

    public static boolean migrateStorageFolder(Context context) {
        File inputDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Snapseed");
        if (!inputDirectory.exists()) {
            return true;
        }
        File outputDirectory = new File(Environment.getExternalStorageDirectory(), "Snapseed");
        if (!outputDirectory.exists() && !outputDirectory.mkdirs()) {
            return $assertionsDisabled;
        }
        boolean migrationSucceeded = true;
        File[] sourceFiles = inputDirectory.listFiles();
        if (sourceFiles != null) {
            for (File sourceFile : sourceFiles) {
                File destination = new File(outputDirectory, sourceFile.getName());
                int count = 16;
                while (true) {
                    int count2 = count - 1;
                    if (count > 0 && destination.exists()) {
//                        destination = new File(outputDirectory, String.format(Locale.US, "%s_%04d.%s", new Object[]{FileHelper.getFileName(fullFileName), Integer.valueOf((int) (Math.random() * 10000.0d)), FileHelper.getFileExtension(fullFileName)}));
                        count = count2;
                    }
                }
//                if (destination.exists() || !sourceFile.renameTo(destination)) {
//                    migrationSucceeded = $assertionsDisabled;
//                }
            }
        }
        context.sendBroadcast(new Intent("android.intent.action.MEDIA_MOUNTED", Uri.parse("file://" + inputDirectory.getAbsolutePath())));
        context.sendBroadcast(new Intent("android.intent.action.MEDIA_MOUNTED", Uri.parse("file://" + outputDirectory.getAbsolutePath())));
        return (migrationSucceeded && inputDirectory.delete()) ? true : $assertionsDisabled;
    }

    private void trimPhotoSphereData() {
        if (this._metadata != null) {
            String xmpContent = this._metadata.getXmpSection();
            if (xmpContent != null && xmpContent.contains("GPano")) {
                if (xmpContent.contains("?xpacket")) {
                    xmpContent = xmpContent.replaceAll("\\s*<\\?\\s*xpacket.+\\?>\\s*", "");
                }
                Document xmpDom = XmlHelper.parseXml(xmpContent);
                Node rdfDescNode = XmlHelper.lookupDocumentNode(xmpDom, "rdf:Description");
                if (rdfDescNode != null) {
                    int i;
                    Iterator i$;
                    if (rdfDescNode instanceof Element) {
                        ArrayList<String> gpanoAttrs = new ArrayList();
                        NamedNodeMap attrMap = rdfDescNode.getAttributes();
                        int attrCount = attrMap != null ? attrMap.getLength() : 0;
                        for (i = 0; i < attrCount; i++) {
                            String nodeName = attrMap.item(i).getNodeName();
                            if (nodeName.startsWith("GPano:") || nodeName.endsWith(":GPano")) {
                                gpanoAttrs.add(nodeName);
                            }
                        }
                        i$ = gpanoAttrs.iterator();
                        while (i$.hasNext()) {
                            ((Element) rdfDescNode).removeAttribute((String) i$.next());
                        }
                    }
                    ArrayList<Node> gpanoNodes = new ArrayList();
                    NodeList childNodes = rdfDescNode.getChildNodes();
                    int childCount = childNodes != null ? childNodes.getLength() : 0;
                    for (i = 0; i < childCount; i++) {
                        Node childNode = childNodes.item(i);
                        if (childNode.getNodeName().startsWith("GPano:")) {
                            gpanoNodes.add(childNode);
                        }
                    }
                    i$ = gpanoNodes.iterator();
                    while (i$.hasNext()) {
                        Node node = (Node) i$.next();
                        Node nextNode = node.getNextSibling();
                        if (nextNode != null && nextNode.getNodeType() == (short) 3 && nextNode.getTextContent().trim().isEmpty()) {
                            rdfDescNode.removeChild(nextNode);
                        }
                        rdfDescNode.removeChild(node);
                    }
                    this._metadata.updateXmpSection(XmlHelper.generateXmlString(xmpDom));
                }
            }
        }
    }
}

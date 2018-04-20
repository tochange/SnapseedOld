package com.niksoftware.snapseed.util;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Environment;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileHelper {
    private static final int DEFAULT_COPY_BUFFER_SIZE = 65536;

    private static class Mutex {
        private boolean _locked;

        private Mutex() {
            this._locked = true;
        }

        public synchronized void waitForUnlock() {
            while (this._locked) {
                try {
                    wait();
                } catch (InterruptedException e) {
                }
            }
        }

        public synchronized void unlock() {
            this._locked = false;
            notify();
        }
    }

    public static String getFilePath(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }
        String filePath = getFileData(context, uri, "_data");
        if (filePath == null) {
            return getFileDisplayNameFull(context, uri);
        }
        return filePath;
    }

    public static String getFileData(Context context, Uri uri, String column) {
        String str = null;
        if (!(context == null || uri == null)) {
            Cursor cursor = null;
            try {
                cursor = new CursorLoader(context, uri, new String[]{column}, null, null, null).loadInBackground();
                cursor.moveToFirst();
                str = cursor.getString(cursor.getColumnIndexOrThrow(column));
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
                str = "";
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return str;
    }

    private static String getFileDisplayNameFull(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }
        return getFileData(context, uri, "_display_name");
    }

    public static String getFileDisplayName(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }
        String fileName = getFileData(context, uri, "_display_name");
        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }
        if (fileName == null) {
            return fileName;
        }
        int index = fileName.lastIndexOf(46);
        if (index != -1) {
            return fileName.substring(0, index);
        }
        return fileName;
    }

    public static String getFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return filePath;
        }
        int fileNameCharIndex = filePath.lastIndexOf(47);
        String fileName = fileNameCharIndex == -1 ? filePath : filePath.substring(fileNameCharIndex);
        int extensionCharIndex = fileName.lastIndexOf(46);
        return extensionCharIndex != -1 ? fileName.substring(0, extensionCharIndex) : fileName;
    }

    public static String getFileExtension(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return filePath;
        }
        int extensionCharIndex = filePath.lastIndexOf(46);
        return extensionCharIndex != -1 ? filePath.substring(extensionCharIndex + 1) : "";
    }

    public static boolean copyFile(String sourcePath, String destinationPath) {
        return copyFile(sourcePath, destinationPath, DEFAULT_COPY_BUFFER_SIZE);
    }

    public static boolean copyFile(String sourcePath, String destinationPath, int bufferSize) {
        try {
            FileInputStream sourceStream = new FileInputStream(new File(sourcePath));
            FileOutputStream destinationStream = new FileOutputStream(new File(destinationPath));
            boolean result = copyStream(sourceStream, destinationStream, bufferSize);
            destinationStream.close();
            sourceStream.close();
            return result;
        } catch (IOException e) {
            return false;
        }
    }

    public static boolean copyStream(InputStream sourceStream, OutputStream destinationStream) {
        return copyStream(sourceStream, destinationStream, DEFAULT_COPY_BUFFER_SIZE);
    }

    public static boolean copyStream(InputStream sourceStream, OutputStream destinationStream, int bufferSize) {
        try {
            byte[] buffer = new byte[bufferSize];
            while (true) {
                int bytesRead = sourceStream.read(buffer);
                if (bytesRead <= 0) {
                    return true;
                }
                destinationStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            return false;
        }
    }

    public static Uri scanMediaFile(Context context, String filePath) {
        final AnonymousClass1ScanResult result = new Object() {
            public Uri _uri;
        };
        final Mutex scanMutex = new Mutex();
        MediaScannerConnection.scanFile(context, new String[]{filePath}, null, new OnScanCompletedListener() {
            public void onScanCompleted(String path, Uri uri) {
                result._uri = uri;
                scanMutex.unlock();
            }
        });
        scanMutex.waitForUnlock();
        return result._uri;
    }

    public static boolean isSDCardWritable() {
        return Environment.getExternalStorageState().equals("mounted");
    }

    public static boolean isSDCardReadable() {
        String state = Environment.getExternalStorageState();
        return state.equals("mounted") || state.equals("mounted_ro");
    }
}

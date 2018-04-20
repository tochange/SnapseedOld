package com.niksoftware.snapseed.util;

import android.graphics.Matrix;

public class ExifData {
    public static final int ORIENTATION_FLIP_HORIZONTAL = 2;
    public static final int ORIENTATION_FLIP_VERTICAL = 4;
    public static final int ORIENTATION_NORMAL = 1;
    public static final int ORIENTATION_ROTATE_180 = 3;
    public static final int ORIENTATION_ROTATE_270 = 8;
    public static final int ORIENTATION_ROTATE_90 = 6;
    public static final int ORIENTATION_TRANSPOSE = 5;
    public static final int ORIENTATION_TRANSVERSE = 7;
    public static final int ORIENTATION_UNDEFINED = 0;
    public static final short TAG_COPYRIGHT = (short) -32104;
    public static final short TAG_DATE_TIME = (short) 306;
    public static final short TAG_EXIF_IMAGE_HEIGHT = (short) -24573;
    public static final short TAG_EXIF_IMAGE_WIDTH = (short) -24574;
    public static final short TAG_EXIF_OFFSET = (short) -30871;
    public static final short TAG_EXIF_VERSION = (short) -28672;
    public static final short TAG_GPS_ALTITUDE = (short) 6;
    public static final short TAG_GPS_ALTITUDE_REF = (short) 5;
    public static final short TAG_GPS_IFD = (short) -30683;
    public static final short TAG_GPS_LATITUDE = (short) 2;
    public static final short TAG_GPS_LATITUDE_REF = (short) 1;
    public static final short TAG_GPS_LONGITUDE = (short) 4;
    public static final short TAG_GPS_LONGITUDE_REF = (short) 3;
    public static final short TAG_GPS_TIMESTAMP = (short) 7;
    public static final short TAG_GPS_VERSION = (short) 0;
    public static final short TAG_IMAGE_DESC = (short) 270;
    public static final short TAG_IMAGE_HEIGHT = (short) 257;
    public static final short TAG_IMAGE_WIDTH = (short) 256;
    public static final short TAG_INTEROP_IFD = (short) -24571;
    public static final short TAG_MAKE = (short) 271;
    public static final short TAG_MAKERNOTE_IFD = (short) -28036;
    public static final short TAG_MODEL = (short) 272;
    public static final short TAG_ORIENTATION = (short) 274;
    public static final short TAG_PRIMARY_CHROM = (short) 319;
    public static final short TAG_REF_BLACK_WHITE = (short) 532;
    public static final short TAG_RESOLUTION_UNIT = (short) 296;
    public static final short TAG_SOFTWARE = (short) 305;
    public static final short TAG_WHITE_POINT = (short) 318;
    public static final short TAG_X_RESOLUTION = (short) 282;
    public static final short TAG_Y_CB_CR_COEF = (short) 529;
    public static final short TAG_Y_CB_CR_POS = (short) 531;
    public static final short TAG_Y_RESOLUTION = (short) 283;
    public static final int WHITEBALANCE_AUTO = 0;
    public static final int WHITEBALANCE_MANUAL = 1;

    public static Matrix getTransformationMatrix(int exifOrientation, int imageWidth, int imageHeight) {
        Matrix matrix = new Matrix();
        switch (exifOrientation) {
            case 2:
                matrix.postScale(-1.0f, 1.0f);
                matrix.postTranslate((float) imageWidth, 0.0f);
                break;
            case 3:
                matrix.postRotate(180.0f);
                break;
            case 4:
                matrix.postScale(1.0f, -1.0f);
                matrix.postTranslate(0.0f, (float) imageHeight);
                break;
            case 5:
                matrix.postRotate(270.0f);
                matrix.postScale(-1.0f, 1.0f);
                matrix.postTranslate((float) imageWidth, 0.0f);
                break;
            case 6:
                matrix.postRotate(90.0f);
                break;
            case 7:
                matrix.postRotate(90.0f);
                matrix.postScale(-1.0f, 1.0f);
                matrix.postTranslate((float) imageWidth, 0.0f);
                break;
            case 8:
                matrix.postRotate(270.0f);
                break;
        }
        return matrix;
    }
}

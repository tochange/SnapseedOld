package com.niksoftware.snapseed.util;

import android.content.ContentResolver;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class MetaData {
    static final /* synthetic */ boolean $assertionsDisabled = (!MetaData.class.desiredAssertionStatus() ? true : $assertionsDisabled);
    private static final String EXIF_DATA_HEADER = "Exif\u0000\u0000";
    private static final String EXIF_VERSION = "0210";
    private static final HashSet<Short> GPS_IFD_TAGS = createShortSet(new Short[]{Short.valueOf((short) 0), Short.valueOf((short) 1), Short.valueOf((short) 2), Short.valueOf((short) 3), Short.valueOf((short) 4), Short.valueOf((short) 5), Short.valueOf((short) 6), Short.valueOf((short) 7)});
    private static final HashSet<Short> IFD0_TAGS = createShortSet(new Short[]{Short.valueOf(ExifData.TAG_IMAGE_DESC), Short.valueOf(ExifData.TAG_MAKE), Short.valueOf(ExifData.TAG_MODEL), Short.valueOf(ExifData.TAG_ORIENTATION), Short.valueOf(ExifData.TAG_X_RESOLUTION), Short.valueOf(ExifData.TAG_Y_RESOLUTION), Short.valueOf(ExifData.TAG_RESOLUTION_UNIT), Short.valueOf(ExifData.TAG_SOFTWARE), Short.valueOf(ExifData.TAG_DATE_TIME), Short.valueOf(ExifData.TAG_WHITE_POINT), Short.valueOf(ExifData.TAG_PRIMARY_CHROM), Short.valueOf(ExifData.TAG_Y_CB_CR_COEF), Short.valueOf(ExifData.TAG_Y_CB_CR_POS), Short.valueOf(ExifData.TAG_REF_BLACK_WHITE), Short.valueOf(ExifData.TAG_COPYRIGHT), Short.valueOf(ExifData.TAG_EXIF_OFFSET)});
    private static final int MARKER_APP0 = 65504;
    private static final int MARKER_APP1 = 65505;
    private static final int MARKER_APP12 = 65516;
    private static final int MARKER_APP13 = 65517;
    private static final int MARKER_APP14 = 65518;
    private static final int MARKER_APP2 = 65506;
    private static final int MARKER_APP3 = 65507;
    private static final int MARKER_DHT = 65476;
    private static final int MARKER_DQT = 65499;
    private static final int MARKER_DRI = 65501;
    private static final int MARKER_EOI = 65497;
    private static final int MARKER_SOF = 65472;
    private static final int MARKER_SOI = 65496;
    private static final int MARKER_SOS = 65498;
    private static final HashSet<Short> SECTIONS_TO_PRESERVE = createShortSet(new Short[]{Short.valueOf((short) -32), Short.valueOf((short) -31), Short.valueOf((short) -30), Short.valueOf((short) -29), Short.valueOf((short) -20), Short.valueOf((short) -19), Short.valueOf((short) -18)});
    private static final HashSet<Short> SUB_IFD_TAGS = createShortSet(new Short[]{Short.valueOf(ExifData.TAG_EXIF_VERSION), Short.valueOf(ExifData.TAG_EXIF_IMAGE_WIDTH), Short.valueOf(ExifData.TAG_EXIF_IMAGE_HEIGHT), Short.valueOf(ExifData.TAG_MAKERNOTE_IFD)});
    private static final String XMP_SECTION_HEADER = "http://ns.adobe.com/xap/1.0/\u0000";
    private ByteOrder _byteOrder = ByteOrder.LITTLE_ENDIAN;
    private ImageFileDirectory _gpsIfd;
    private ImageFileDirectory _ifd0;
    private ImageFileDirectory _ifd1;
    private ImageFileDirectory _interopIfd;
    private byte[] _makernoteIfdBlob;
    private ArrayList<MetadataSection> _sections = new ArrayList();
    private ImageFileDirectory _subIfd;

    private static class ImageFileDirectory extends ArrayList<Tag> {
        static final /* synthetic */ boolean $assertionsDisabled = (!MetaData.class.desiredAssertionStatus() ? true : $assertionsDisabled);
        private static final int DEFAULT_CAPACITY = 16;
        public int nextDirectoryOffset;

        private ImageFileDirectory() {
            this(16);
        }

        private ImageFileDirectory(int capacity) {
            super(capacity);
            this.nextDirectoryOffset = -1;
        }

        public static ImageFileDirectory parseDirectoryData(ByteBuffer directoryData, Map<Short, Integer> directoryTagsToExtract) {
            ImageFileDirectory directory = new ImageFileDirectory();
            short entryCount = directoryData.getShort();
            while (true) {
                short entryCount2 = (short) (entryCount - 1);
                if (entryCount > (short) 0) {
                    short tagId = directoryData.getShort();
                    short tagFormat = directoryData.getShort();
                    int componentCount = directoryData.getInt();
                    Tag tagData = new Tag();
                    tagData.id = tagId;
                    tagData.format = tagFormat;
                    tagData.componentCount = componentCount;
                    int tagDataSize = componentCount * Tag.getComponentSize(tagFormat);
                    if (directoryTagsToExtract != null && directoryTagsToExtract.containsKey(Short.valueOf(tagId))) {
                        directoryTagsToExtract.put(Short.valueOf(tagId), Integer.valueOf(directoryData.getInt()));
                        tagData.data = new byte[4];
                    } else if (tagDataSize <= 4) {
                        tagData.data = new byte[4];
                        directoryData.get(tagData.data);
                    } else {
                        int dataOffset = directoryData.getInt();
                        int currentPosition = directoryData.position();
                        directoryData.position(dataOffset + 6);
                        tagData.data = new byte[tagDataSize];
                        directoryData.get(tagData.data);
                        directoryData.position(currentPosition);
                    }
                    directory.add(tagData);
                    entryCount = entryCount2;
                } else {
                    directory.nextDirectoryOffset = directoryData.getInt();
                    return directory;
                }
            }
        }

        public int generateDirectoryData(ByteBuffer outDirectoryData, Map<Short, Integer> directoryTagsToExtract) {
            if ($assertionsDisabled || outDirectoryData != null) {
                int currentDataOffset = outDirectoryData.position() + headerSize();
                if ($assertionsDisabled || dataSize() + currentDataOffset <= outDirectoryData.capacity()) {
                    outDirectoryData.putShort((short) size());
                    Collections.sort(this, new Comparator<Tag>() {
                        public int compare(Tag tag, Tag tag2) {
                            return (tag.id & 65535) - (tag2.id & 65535);
                        }
                    });
                    Iterator i$ = iterator();
                    while (i$.hasNext()) {
                        Tag tag = (Tag) i$.next();
                        if (!$assertionsDisabled && (tag.data == null || tag.data.length < 4)) {
                            throw new AssertionError("Invalid tag data");
                        } else if (tag.id != ExifData.TAG_MAKERNOTE_IFD) {
                            outDirectoryData.putShort(tag.id);
                            outDirectoryData.putShort(tag.format);
                            outDirectoryData.putInt(tag.componentCount);
                            if (directoryTagsToExtract != null && directoryTagsToExtract.containsKey(Short.valueOf(tag.id))) {
                                directoryTagsToExtract.put(Short.valueOf(tag.id), Integer.valueOf(outDirectoryData.position()));
                            }
                            if (tag.data.length == 4) {
                                outDirectoryData.put(tag.data);
                            } else {
                                outDirectoryData.putInt(currentDataOffset);
                                int currentPosition = outDirectoryData.position();
                                outDirectoryData.position(currentDataOffset);
                                outDirectoryData.put(tag.data);
                                currentDataOffset += tag.data.length;
                                outDirectoryData.position(currentPosition);
                            }
                        }
                    }
                    int nextDirectoryOffsetPosition = outDirectoryData.position();
                    outDirectoryData.putInt(0);
                    outDirectoryData.position(currentDataOffset);
                    return nextDirectoryOffsetPosition;
                }
                throw new AssertionError("Buffer is too small to fit data");
            }
            throw new AssertionError("Invalid input parameter");
        }

        public Tag getTag(short tagId) {
            Iterator i$ = iterator();
            while (i$.hasNext()) {
                Tag tag = (Tag) i$.next();
                if (tag.id == tagId) {
                    return tag;
                }
            }
            return null;
        }

        public int headerSize() {
            return (size() * 12) + 6;
        }

        public int dataSize() {
            int dataSize = 0;
            Iterator i$ = iterator();
            while (i$.hasNext()) {
                Tag tag = (Tag) i$.next();
                if (tag.data.length > 4) {
                    dataSize += tag.data.length;
                }
            }
            return dataSize;
        }

        public int totalSize() {
            return headerSize() + dataSize();
        }
    }

    private static class MetadataSection {
        public ByteBuffer data;
        public short id;
        public short size;

        public MetadataSection(short id, short size, ByteBuffer data) {
            this.id = id;
            this.size = size;
            this.data = data;
        }
    }

    private static class Tag {
        static final /* synthetic */ boolean $assertionsDisabled = (!MetaData.class.desiredAssertionStatus() ? true : $assertionsDisabled);
        public static final int FORMAT_ASCII_STRING = 2;
        public static final int FORMAT_DOUBLE_FLOAT = 12;
        public static final int FORMAT_SIGNED_BYTE = 6;
        public static final int FORMAT_SIGNED_LONG = 9;
        public static final int FORMAT_SIGNED_RATIONAL = 10;
        public static final int FORMAT_SIGNED_SHORT = 8;
        public static final int FORMAT_SINGLE_FLOAT = 11;
        public static final int FORMAT_UNDEFINED = 7;
        private static final int FORMAT_UNKNOWN = -1;
        public static final int FORMAT_UNSIGNED_BYTE = 1;
        public static final int FORMAT_UNSIGNED_LONG = 4;
        public static final int FORMAT_UNSIGNED_RATIONAL = 5;
        public static final int FORMAT_UNSIGNED_SHORT = 3;
        public int componentCount;
        public byte[] data;
        public short format;
        public short id;

        private Tag() {
            this.format = (short) -1;
        }

        public static int getComponentSize(int tagFormat) {
            switch (tagFormat) {
                case 1:
                case 2:
                case 6:
                case 7:
                    return 1;
                case 3:
                    return 2;
                case 4:
                    return 4;
                case 5:
                    return 8;
                case 8:
                    return 2;
                case 9:
                    return 4;
                case 10:
                    return 8;
                case 11:
                    return 4;
                case 12:
                    return 8;
                default:
                    return 0;
            }
        }

        public String getString() {
            return this.format == (short) 2 ? (String) getValue(this.data, 2, ByteOrder.LITTLE_ENDIAN) : null;
        }

        public Integer getInteger(ByteOrder byteOrder) {
            return (this.format == (short) 1 || this.format == (short) 3 || this.format == (short) 4 || this.format == (short) 6 || this.format == (short) 8 || this.format == (short) 9) ? (Integer) getValue(this.data, this.format, byteOrder) : null;
        }

        public Integer[] getIntegerArray(ByteOrder byteOrder) {
            return (this.format == (short) 1 || this.format == (short) 3 || this.format == (short) 4 || this.format == (short) 6 || this.format == (short) 8 || this.format == (short) 9) ? (Integer[]) getValueArray(this.data, this.format, byteOrder, this.componentCount) : null;
        }

        public Rational getRational(ByteOrder byteOrder) {
            return (this.format == (short) 5 || this.format == (short) 10) ? (Rational) getValue(this.data, this.format, byteOrder) : null;
        }

        public Rational[] getRationalArray(ByteOrder byteOrder) {
            return (this.format == (short) 5 || this.format == (short) 10) ? (Rational[]) getValueArray(this.data, this.format, byteOrder, this.componentCount) : null;
        }

        public boolean setString(String value) {
            int i = 4;
            if (value == null) {
                return $assertionsDisabled;
            }
            this.format = (short) 2;
            byte[] stringBytes = value.getBytes();
            if (stringBytes.length + 1 >= 4) {
                i = stringBytes.length + 1;
            }
            this.data = new byte[i];
            System.arraycopy(stringBytes, 0, this.data, 0, stringBytes.length);
            this.componentCount = this.data.length;
            return true;
        }

        public boolean setInteger(Integer value, int valueFormat, ByteOrder byteOrder) {
            if (value == null) {
                return $assertionsDisabled;
            }
            this.format = (short) valueFormat;
            this.componentCount = 1;
            if (this.data == null || this.data.length != 4) {
                this.data = new byte[4];
            }
            ByteBuffer buffer = ByteBuffer.wrap(this.data).order(byteOrder);
            switch (valueFormat) {
                case 1:
                case 6:
                    buffer.put((byte) value.intValue());
                    buffer.put(new byte[]{(byte) 0, (byte) 0, (byte) 0});
                    break;
                case 3:
                case 8:
                    buffer.putShort((short) value.intValue());
                    buffer.putShort((short) 0);
                    break;
                case 4:
                case 9:
                    buffer.putInt(value.intValue());
                    break;
                default:
                    if (!$assertionsDisabled) {
                        throw new AssertionError("Invalid integer tag value format");
                    }
                    break;
            }
            return true;
        }

        public boolean setDate(Date value) {
            if (value == null) {
                return $assertionsDisabled;
            }
            this.format = (short) 2;
            this.componentCount = 20;
            this.data = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss\u0000").format(value).getBytes();
            return true;
        }

        public boolean setData(byte[] value) {
            if (value == null || value.length < 1) {
                return $assertionsDisabled;
            }
            this.format = (short) 7;
            this.componentCount = value.length;
            this.data = value;
            return true;
        }

        private static Object getValue(byte[] tagData, int tagFormat, ByteOrder byteOrder) {
            switch (tagFormat) {
                case 1:
                case 6:
                    return Integer.valueOf(ByteBuffer.wrap(tagData).order(byteOrder).get());
                case 2:
                    int zeroIndex = 0;
                    while (tagData[zeroIndex] != (byte) 0) {
                        zeroIndex++;
                    }
                    return new String(tagData, 0, zeroIndex);
                case 3:
                case 8:
                    return Integer.valueOf(ByteBuffer.wrap(tagData).order(byteOrder).getShort());
                case 4:
                case 9:
                    return Integer.valueOf(ByteBuffer.wrap(tagData).order(byteOrder).getInt());
                case 5:
                case 10:
                    ByteBuffer rationalBuffer = ByteBuffer.wrap(tagData).order(byteOrder);
                    return new Rational(Integer.valueOf(rationalBuffer.getInt()), Integer.valueOf(rationalBuffer.getInt()));
                case 7:
                    return String.format("undefined (%d bytes)", new Object[]{Integer.valueOf(tagData.length)});
                case 11:
                    return Float.valueOf(ByteBuffer.wrap(tagData).order(byteOrder).getFloat());
                case 12:
                    return Double.valueOf(ByteBuffer.wrap(tagData).order(byteOrder).getDouble());
                default:
                    return null;
            }
        }

        private static Object[] getValueArray(byte[] tagData, int tagFormat, ByteOrder byteOrder, int valueCount) {
            if (valueCount < 1) {
                return null;
            }
            Object[] result;
            int i;
            switch (tagFormat) {
                case 1:
                case 6:
                    result = new Integer[valueCount];
                    ByteBuffer byteBuffer = ByteBuffer.wrap(tagData).order(byteOrder);
                    for (i = 0; i < valueCount; i++) {
                        result[i] = Integer.valueOf(byteBuffer.get());
                    }
                    return result;
                case 2:
                    int zeroIndex;
                    for (zeroIndex = 0; tagData[zeroIndex] != (byte) 0; zeroIndex++) {
                    }
                    return new Object[]{new String(tagData, 0, zeroIndex)};
                case 3:
                case 8:
                    result = new Integer[valueCount];
                    ByteBuffer shortBuffer = ByteBuffer.wrap(tagData).order(byteOrder);
                    for (i = 0; i < valueCount; i++) {
                        result[i] = Integer.valueOf(shortBuffer.getShort());
                    }
                    return result;
                case 4:
                case 9:
                    result = new Integer[valueCount];
                    ByteBuffer intBuffer = ByteBuffer.wrap(tagData).order(byteOrder);
                    for (i = 0; i < valueCount; i++) {
                        result[i] = Integer.valueOf(intBuffer.getInt());
                    }
                    return result;
                case 5:
                case 10:
                    result = new Rational[valueCount];
                    ByteBuffer rationalBuffer = ByteBuffer.wrap(tagData).order(byteOrder);
                    for (i = 0; i < valueCount; i++) {
                        result[i] = new Rational(Integer.valueOf(rationalBuffer.getInt()), Integer.valueOf(rationalBuffer.getInt()));
                    }
                    return result;
                case 11:
                    result = new Float[valueCount];
                    ByteBuffer floatBuffer = ByteBuffer.wrap(tagData).order(byteOrder);
                    for (i = 0; i < valueCount; i++) {
                        result[i] = Float.valueOf(floatBuffer.getFloat());
                    }
                    return result;
                case 12:
                    result = new Double[valueCount];
                    ByteBuffer doubleBuffer = ByteBuffer.wrap(tagData).order(byteOrder);
                    for (i = 0; i < valueCount; i++) {
                        result[i] = Double.valueOf(doubleBuffer.getDouble());
                    }
                    return result;
                default:
                    return null;
            }
        }

        public String toString() {
            Object value = getValue(this.data, this.format, ByteOrder.LITTLE_ENDIAN);
            String str = "{ id: 0x%04x, value: \"%s\" }";
            Object[] objArr = new Object[2];
            objArr[0] = Short.valueOf(this.id);
            objArr[1] = value != null ? value.toString() : "null";
            return String.format(str, objArr);
        }
    }

    private MetaData() {
    }

    public static MetaData load(String imagePath) {
        InputStream imageStream;
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }
        Date imageCreationDate = null;
        try {
            File imageFile = new File(imagePath);
            Date imageCreationDate2 = new Date(imageFile.lastModified());
            try {
                imageStream = new FileInputStream(imageFile);
                imageCreationDate = imageCreationDate2;
            } catch (IOException e) {
                imageCreationDate = imageCreationDate2;
                imageStream = null;
                return load(imageStream, imageCreationDate);
            }
        } catch (IOException e2) {
            imageStream = null;
            return load(imageStream, imageCreationDate);
        }
        return load(imageStream, imageCreationDate);
    }

    public static MetaData load(ContentResolver contentResolver, Uri imageUri) {
        InputStream imageStream;
        try {
            imageStream = contentResolver.openInputStream(imageUri);
        } catch (Exception e) {
            imageStream = null;
        }
        return load(imageStream, null);
    }

    private static MetaData load(InputStream imageStream, Date imageCreationDate) {
        if (imageStream == null) {
            return null;
        }
        MetaData metaData = new MetaData();
        boolean exifDataPresent = $assertionsDisabled;
        boolean loadSucceeded = true;
        try {
            BufferedInputStream inputStream = new BufferedInputStream(imageStream);
            inputStream.mark(65536);
            if (readWord(inputStream) == MARKER_SOI) {
                while (loadSucceeded && inputStream.available() > 0) {
                    int sectionId = readWord(inputStream);
                    if ((65280 & sectionId) == 65280 && sectionId != MARKER_SOS && sectionId != MARKER_EOI) {
                        int sectionLength = readWord(inputStream);
                        if (sectionLength == 0) {
                            break;
                        }
                        sectionLength -= 2;
                        ByteBuffer sectionData = ByteBuffer.allocate(sectionLength);
                        loadSucceeded = inputStream.read(sectionData.array()) == sectionLength ? true : $assertionsDisabled;
                        if (loadSucceeded && sectionId == MARKER_APP1 && metaData.parseExifSegment(sectionData)) {
                            if ($assertionsDisabled || exifDataPresent) {
                                exifDataPresent = true;
                                sectionData = null;
                            } else {
                                throw new AssertionError("Reoccurring APP1 section with Exif data");
                            }
                        }
                        if (sectionData != null && isSectionToPreserve(sectionId)) {
                            metaData._sections.add(new MetadataSection((short) sectionId, (short) sectionLength, sectionData));
                        }
                    } else {
                        break;
                    }
                }
            }
            if (loadSucceeded && !exifDataPresent) {
                inputStream.reset();
                Options options = new Options();
                options.inPreferredConfig = Config.ARGB_8888;
                options.inScaled = $assertionsDisabled;
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, options);
                if (imageCreationDate == null) {
                    imageCreationDate = new Date();
                }
                metaData = create(imageCreationDate, options.outWidth, options.outHeight);
            }
            inputStream.close();
        } catch (IOException e) {
            loadSucceeded = $assertionsDisabled;
        }
        if (loadSucceeded) {
            return metaData;
        }
        return null;
    }

    public static MetaData create(Date creationDate, int width, int height) {
        MetaData metaData = new MetaData();
        metaData.setExifDate(ExifData.TAG_DATE_TIME, creationDate);
        metaData.setExifInteger(ExifData.TAG_ORIENTATION, Integer.valueOf(1));
        metaData.setExifData(ExifData.TAG_EXIF_VERSION, EXIF_VERSION.getBytes());
        metaData.setExifInteger(ExifData.TAG_EXIF_IMAGE_WIDTH, Integer.valueOf(width));
        metaData.setExifInteger(ExifData.TAG_EXIF_IMAGE_HEIGHT, Integer.valueOf(height));
        return metaData;
    }

    public boolean save(String imagePath) {
        boolean saveSucceeded = $assertionsDisabled;
        try {
            File inputFile = new File(imagePath);
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(inputFile));
            File tempFile = new File(imagePath + ".~~~");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            if (readWord(inputStream) == MARKER_SOI) {
                writeWord(outputStream, MARKER_SOI);
                writeWord(outputStream, MARKER_APP1);
                ByteBuffer exifSegment = generateExifSegment();
                writeWord(outputStream, exifSegment.position() + 8);
                outputStream.write(EXIF_DATA_HEADER.getBytes());
                outputStream.write(exifSegment.array(), 0, exifSegment.position());
                Iterator i$ = this._sections.iterator();
                while (i$.hasNext()) {
                    MetadataSection section = (MetadataSection) i$.next();
                    writeWord(outputStream, section.id);
                    writeWord(outputStream, section.size + 2);
                    outputStream.write(section.data.array());
                }
                byte[] readBuffer = new byte[1024];
                saveSucceeded = true;
                while (inputStream.available() > 0) {
                    int sectionId = readWord(inputStream);
                    if ((65280 & sectionId) != 65280 || sectionId == MARKER_SOS || sectionId == MARKER_EOI) {
                        writeWord(outputStream, sectionId);
                        break;
                    }
                    int sectionLength = readWord(inputStream);
                    if (isSectionToPreserve(sectionId)) {
                        saveSucceeded = inputStream.skip((long) (sectionLength + -2)) == ((long) (sectionLength + -2)) ? true : $assertionsDisabled;
                    } else {
                        writeWord(outputStream, sectionId);
                        writeWord(outputStream, sectionLength);
                        sectionLength -= 2;
                        while (saveSucceeded && sectionLength > 0) {
                            int bytesRead = inputStream.read(readBuffer, 0, Math.min(sectionLength, 1024));
                            saveSucceeded = bytesRead > 0 ? true : $assertionsDisabled;
                            if (saveSucceeded) {
                                outputStream.write(readBuffer, 0, bytesRead);
                                sectionLength -= bytesRead;
                            }
                        }
                    }
                }
                while (saveSucceeded && inputStream.available() > 0) {
                    outputStream.write(readBuffer, 0, inputStream.read(readBuffer));
                }
            }
            inputStream.close();
            outputStream.close();
            saveSucceeded = (saveSucceeded && tempFile.renameTo(inputFile)) ? true : $assertionsDisabled;
            return saveSucceeded;
        } catch (IOException e) {
            return $assertionsDisabled;
        }
    }

    public String getExifString(short tagId) {
        Tag tag = findTag(tagId, $assertionsDisabled);
        return tag != null ? tag.getString() : null;
    }

    public Integer getExifInteger(short tagId) {
        Tag tag = findTag(tagId, $assertionsDisabled);
        return tag != null ? tag.getInteger(this._byteOrder) : null;
    }

    public Integer[] getExifIntegerArray(short tagId) {
        Tag tag = findTag(tagId, $assertionsDisabled);
        return tag != null ? tag.getIntegerArray(this._byteOrder) : null;
    }

    public Rational getExifRational(short tagId) {
        Tag tag = findTag(tagId, $assertionsDisabled);
        return tag != null ? tag.getRational(this._byteOrder) : null;
    }

    public Rational[] getExifRationalArray(short tagId) {
        Tag tag = findTag(tagId, $assertionsDisabled);
        return tag != null ? tag.getRationalArray(this._byteOrder) : null;
    }

    public boolean setExifString(short tagId, String value) {
        Tag tag = findTag(tagId, true);
        if (tag == null || !tag.setString(value)) {
            return $assertionsDisabled;
        }
        return true;
    }

    public boolean setExifInteger(short tagId, Integer value) {
        Tag tag = findTag(tagId, true);
        if (tag == null || !tag.setInteger(value, 4, this._byteOrder)) {
            return $assertionsDisabled;
        }
        return true;
    }

    public boolean setExifShort(short tagId, Integer value) {
        Tag tag = findTag(tagId, true);
        if (tag == null || !tag.setInteger(value, 3, this._byteOrder)) {
            return $assertionsDisabled;
        }
        return true;
    }

    public boolean setExifByte(short tagId, Integer value) {
        Tag tag = findTag(tagId, true);
        if (tag == null || !tag.setInteger(value, 1, this._byteOrder)) {
            return $assertionsDisabled;
        }
        return true;
    }

    public boolean setExifDate(short tagId, Date value) {
        Tag tag = findTag(tagId, true);
        if (tag == null || !tag.setDate(value)) {
            return $assertionsDisabled;
        }
        return true;
    }

    public boolean setExifData(short tagId, byte[] value) {
        Tag tag = findTag(tagId, true);
        if (tag == null || !tag.setData(value)) {
            return $assertionsDisabled;
        }
        return true;
    }

    public boolean removeExifAttribute(short tagId) {
        ImageFileDirectory directory = findTagDirectory(tagId, $assertionsDisabled);
        if (directory == null) {
            return $assertionsDisabled;
        }
        Tag tag = directory.getTag(tagId);
        if (tag == null) {
            return $assertionsDisabled;
        }
        directory.remove(tag);
        return true;
    }

    public boolean updateXmpSection(int imageWidth, int imageHeight, int imageOrientation) {
        short xmpSectionIdLength = XMP_SECTION_HEADER.length();
        Iterator i$ = this._sections.iterator();
        while (i$.hasNext()) {
            MetadataSection section = (MetadataSection) i$.next();
            if (section.id == (short) -31 && section.size > xmpSectionIdLength) {
                byte[] sectionData = section.data.array();
                if (XMP_SECTION_HEADER.compareTo(new String(sectionData, 0, xmpSectionIdLength)) == 0) {
                    Charset charset = Charset.forName("UTF-8");
                    section.data = ByteBuffer.wrap(new String(sectionData, charset).replaceAll("tiff:Orientation=\"\\d+\"", String.format("tiff:Orientation=\"%d\"", new Object[]{Integer.valueOf(imageOrientation)})).replaceAll("tiff:ImageWidth=\"\\d+\"", String.format("tiff:ImageWidth=\"%d\"", new Object[]{Integer.valueOf(imageWidth)})).replaceAll("tiff:ImageLength=\"\\d+\"", String.format("tiff:ImageLength=\"%d\"", new Object[]{Integer.valueOf(imageHeight)})).replaceAll("exif:PixelXDimension=\"\\d+\"", String.format("exif:PixelXDimension=\"%d\"", new Object[]{Integer.valueOf(imageWidth)})).replaceAll("exif:PixelYDimension=\"\\d+\"", String.format("exif:PixelYDimension=\"%d\"", new Object[]{Integer.valueOf(imageHeight)})).getBytes(charset));
                    section.size = (short) section.data.capacity();
                }
            }
        }
        return $assertionsDisabled;
    }

    public MetadataSection findSection(short id, String header) {
        int headerLength;
        if (header != null) {
            headerLength = header.length();
        } else {
            headerLength = (short) 0;
        }
        Iterator i$ = this._sections.iterator();
        while (i$.hasNext()) {
            MetadataSection section = (MetadataSection) i$.next();
            if (section.id == (short) -31 && section.size > headerLength) {
                byte[] sectionData = section.data.array();
                if (header == null || header.compareTo(new String(sectionData, 0, headerLength)) == 0) {
                    return section;
                }
            }
        }
        return null;
    }

    public String getXmpSection() {
        MetadataSection section = findSection((short) -31, XMP_SECTION_HEADER);
        if (section == null) {
            return null;
        }
        byte[] sectionData = section.data.array();
        int headerLength = XMP_SECTION_HEADER.length();
        return new String(sectionData, headerLength, sectionData.length - headerLength, Charset.forName("UTF-8"));
    }

    public boolean updateXmpSection(String xmpContent) {
        if (xmpContent == null || xmpContent.isEmpty()) {
            return $assertionsDisabled;
        }
        if (xmpContent.startsWith("<?xml")) {
            xmpContent = xmpContent.replaceFirst("\\s*<\\?\\s*xml\\s.+\\?>\\s*", "");
        }
        MetadataSection section = findSection((short) -31, XMP_SECTION_HEADER);
        if (section != null) {
            Charset charset = Charset.forName("UTF-8");
            section.data = ByteBuffer.allocate(XMP_SECTION_HEADER.length() + xmpContent.length());
            section.data.put(XMP_SECTION_HEADER.getBytes(charset));
            section.data.put(xmpContent.getBytes(charset));
            section.size = (short) section.data.capacity();
        }
        if (section != null) {
            return true;
        }
        return $assertionsDisabled;
    }

    private boolean parseExifSegment(ByteBuffer segmentData) throws IOException {
        segmentData.order(ByteOrder.LITTLE_ENDIAN);
        byte[] header = new byte[6];
        segmentData.get(header);
        if (new String(header).compareTo(EXIF_DATA_HEADER) != 0) {
            return $assertionsDisabled;
        }
        switch (segmentData.getShort()) {
            case (short) 18761:
                this._byteOrder = ByteOrder.LITTLE_ENDIAN;
                break;
            case (short) 19789:
                this._byteOrder = ByteOrder.BIG_ENDIAN;
                break;
            default:
                this._byteOrder = null;
                break;
        }
        if (this._byteOrder == null) {
            return $assertionsDisabled;
        }
        segmentData.order(this._byteOrder);
        if (segmentData.getShort() == (short) 10752 || segmentData.getInt() == 134217728) {
            return $assertionsDisabled;
        }
        this._ifd0 = null;
        this._subIfd = null;
        this._gpsIfd = null;
        this._interopIfd = null;
        this._makernoteIfdBlob = null;
        this._ifd1 = null;
        Map<Short, Integer> directoryTags = new HashMap();
        directoryTags.put(Short.valueOf(ExifData.TAG_EXIF_OFFSET), Integer.valueOf(-1));
        directoryTags.put(Short.valueOf(ExifData.TAG_GPS_IFD), Integer.valueOf(-1));
        directoryTags.put(Short.valueOf(ExifData.TAG_INTEROP_IFD), Integer.valueOf(-1));
        directoryTags.put(Short.valueOf(ExifData.TAG_MAKERNOTE_IFD), Integer.valueOf(-1));
        this._ifd0 = ImageFileDirectory.parseDirectoryData(segmentData, directoryTags);
        if (this._ifd0.nextDirectoryOffset > 0) {
            this._ifd0.nextDirectoryOffset = 0;
        }
        Tag tag = findTag(ExifData.TAG_IMAGE_WIDTH, this._ifd0, $assertionsDisabled);
        if (tag != null) {
            this._ifd0.remove(tag);
        }
        tag = findTag(ExifData.TAG_IMAGE_HEIGHT, this._ifd0, $assertionsDisabled);
        if (tag != null) {
            this._ifd0.remove(tag);
        }
        int ifdOffset = ((Integer) directoryTags.get(Short.valueOf(ExifData.TAG_EXIF_OFFSET))).intValue();
        if (ifdOffset > 0) {
            segmentData.position(ifdOffset + 6);
            this._subIfd = ImageFileDirectory.parseDirectoryData(segmentData, directoryTags);
        }
        ifdOffset = ((Integer) directoryTags.get(Short.valueOf(ExifData.TAG_GPS_IFD))).intValue();
        if (ifdOffset > 0) {
            segmentData.position(ifdOffset + 6);
            this._gpsIfd = ImageFileDirectory.parseDirectoryData(segmentData, directoryTags);
        }
        ifdOffset = ((Integer) directoryTags.get(Short.valueOf(ExifData.TAG_INTEROP_IFD))).intValue();
        if (ifdOffset > 0) {
            segmentData.position(ifdOffset + 6);
            this._interopIfd = ImageFileDirectory.parseDirectoryData(segmentData, directoryTags);
        }
        if (((Integer) directoryTags.get(Short.valueOf(ExifData.TAG_MAKERNOTE_IFD))).intValue() > 0) {
            removeExifAttribute(ExifData.TAG_MAKERNOTE_IFD);
        }
        return true;
    }

    private static boolean isSectionToPreserve(int sectionId) {
        return SECTIONS_TO_PRESERVE.contains(Short.valueOf((short) sectionId));
    }

    private Tag findTag(short tagId, boolean forceCreate) {
        return findTag(tagId, findTagDirectory(tagId, forceCreate), forceCreate);
    }

    private Tag findTag(short tagId, ImageFileDirectory directory, boolean forceCreate) {
        if (directory == null) {
            return null;
        }
        Tag result = directory.getTag(tagId);
        if (result != null || !forceCreate) {
            return result;
        }
        result = new Tag();
        result.id = tagId;
        directory.add(result);
        return result;
    }

    private ImageFileDirectory findTagDirectory(short tagId, boolean forceCreate) {
        if (IFD0_TAGS.contains(Short.valueOf(tagId))) {
            if (forceCreate && this._ifd0 == null) {
                this._ifd0 = new ImageFileDirectory();
            }
            return this._ifd0;
        } else if (SUB_IFD_TAGS.contains(Short.valueOf(tagId))) {
            if (forceCreate && this._subIfd == null) {
                setExifInteger(ExifData.TAG_EXIF_OFFSET, Integer.valueOf(0));
                this._subIfd = new ImageFileDirectory();
            }
            return this._subIfd;
        } else if (!GPS_IFD_TAGS.contains(Short.valueOf(tagId))) {
            return null;
        } else {
            if (forceCreate && this._gpsIfd == null) {
                setExifInteger(ExifData.TAG_GPS_IFD, Integer.valueOf(0));
                this._gpsIfd = new ImageFileDirectory();
            }
            return this._gpsIfd;
        }
    }

    private ByteBuffer generateExifSegment() {
        if (this._byteOrder == null || this._ifd0 == null) {
            return null;
        }
        ByteBuffer exifSegment = ByteBuffer.allocate(65536);
        exifSegment.putShort((short) (this._byteOrder == ByteOrder.LITTLE_ENDIAN ? 18761 : 19789));
        exifSegment.order(this._byteOrder);
        exifSegment.putShort((short) 42);
        exifSegment.putInt(8);
        Map<Short, Integer> directoryTags = new HashMap();
        directoryTags.put(Short.valueOf(ExifData.TAG_EXIF_OFFSET), Integer.valueOf(-1));
        directoryTags.put(Short.valueOf(ExifData.TAG_GPS_IFD), Integer.valueOf(-1));
        directoryTags.put(Short.valueOf(ExifData.TAG_INTEROP_IFD), Integer.valueOf(-1));
        directoryTags.put(Short.valueOf(ExifData.TAG_MAKERNOTE_IFD), Integer.valueOf(-1));
        int ifd1OffsetFieldPosition = this._ifd0.generateDirectoryData(exifSegment, directoryTags);
        if (this._subIfd != null && updateByteBufferInt(exifSegment, ((Integer) directoryTags.get(Short.valueOf(ExifData.TAG_EXIF_OFFSET))).intValue(), exifSegment.position())) {
            this._subIfd.generateDirectoryData(exifSegment, directoryTags);
        }
        if (this._gpsIfd != null && updateByteBufferInt(exifSegment, ((Integer) directoryTags.get(Short.valueOf(ExifData.TAG_GPS_IFD))).intValue(), exifSegment.position())) {
            this._gpsIfd.generateDirectoryData(exifSegment, null);
        }
        if (this._makernoteIfdBlob != null && updateByteBufferInt(exifSegment, ((Integer) directoryTags.get(Short.valueOf(ExifData.TAG_MAKERNOTE_IFD))).intValue(), exifSegment.position())) {
            exifSegment.put(this._makernoteIfdBlob);
        }
        if (this._interopIfd == null || !updateByteBufferInt(exifSegment, ((Integer) directoryTags.get(Short.valueOf(ExifData.TAG_INTEROP_IFD))).intValue(), exifSegment.position())) {
            return exifSegment;
        }
        this._interopIfd.generateDirectoryData(exifSegment, null);
        return exifSegment;
    }

    private static boolean updateByteBufferInt(ByteBuffer buffer, int position, int value) {
        if (position < 0 || position + 4 > buffer.capacity()) {
            return $assertionsDisabled;
        }
        int currentPosition = buffer.position();
        buffer.position(position);
        buffer.putInt(value);
        buffer.position(currentPosition);
        return true;
    }

    private static HashSet<Short> createShortSet(Short[] setElements) {
        if (setElements == null || setElements.length == 0) {
            return null;
        }
        HashSet<Short> result = new HashSet();
        for (Short element : setElements) {
            result.add(element);
        }
        return result;
    }

    private static int readWord(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[2];
        if (inputStream.read(buffer) == 2) {
            return ((buffer[0] << 8) | (buffer[1] & 255)) & 65535;
        }
        throw new IOException("Reading word failed");
    }

    private static void writeWord(OutputStream outputStream, int value) throws IOException {
        outputStream.write((value >> 8) & 255);
        outputStream.write(value & 255);
    }
}

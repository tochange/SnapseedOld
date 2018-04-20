package com.niksoftware.snapseed;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.SimpleAdapter;
import com.niksoftware.snapseed.util.FileHelper;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ImagePropertyActivity extends ListActivity {
    public static final String HEIGHT_EXTRA = "height";
    private static final String ITEM_INFO = "data";
    private static final String ITEM_TITLE = "title";
    public static final String WIDTH_EXTRA = "width";

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.img_property);
        setListAdapter(new SimpleAdapter(this, getPropertyMap(getIntent()), R.layout.image_property_item, new String[]{ITEM_TITLE, ITEM_INFO}, new int[]{R.id.title, R.id.summary}));
    }

    private static Map<String, ?> createInfoItemMap(String propertyTitle, String propertyInfo) {
        Map<String, String> map = new HashMap();
        map.put(ITEM_TITLE, propertyTitle);
        map.put(ITEM_INFO, propertyInfo);
        return Collections.unmodifiableMap(map);
    }

    private List<Map<String, ?>> getPropertyMap(Intent intent) {
        List<Map<String, ?>> list = new ArrayList();
        Uri imageUri = intent.getData();
        File file = null;
        if (imageUri != null) {
            String filePath = FileHelper.getFilePath(this, imageUri);
            if (filePath != null) {
                file = new File(filePath);
            }
        }
        if (file != null) {
            list.add(createInfoItemMap(getString(R.string.property_filename), file.getName()));
        }
        if (file != null) {
            long length = file.length();
            if (length == 0) {
                String size = FileHelper.getFileData(this, imageUri, "_size");
                if (!size.isEmpty()) {
                    length = (long) Integer.parseInt(size);
                }
            }
            list.add(createInfoItemMap(getString(R.string.property_filesize), getFileSizeAsString(length)));
        }
        int width = intent.getIntExtra(WIDTH_EXTRA, 0);
        int height = intent.getIntExtra(HEIGHT_EXTRA, 0);
        list.add(createInfoItemMap(getString(R.string.property_imagesize), String.format("%d x %d", new Object[]{Integer.valueOf(width), Integer.valueOf(height)})));
        return list;
    }

    private String getFileSizeAsString(long size) {
        float length = (float) size;
        String unit = getString(R.string.unit_byte);
        if (length > 1024.0f) {
            length /= 1024.0f;
            unit = getString(R.string.unit_kb);
        }
        if (length > 1024.0f) {
            length /= 1024.0f;
            unit = getString(R.string.unit_mb);
        }
        if (length > 1024.0f) {
            length /= 1024.0f;
            unit = getString(R.string.unit_gb);
        }
        return String.format("%.2f %s", new Object[]{Float.valueOf(length), unit});
    }

    private double degreeToDouble(double degree, double minute, double second) {
        return ((minute / 60.0d) + degree) + (second / 3600.0d);
    }
}

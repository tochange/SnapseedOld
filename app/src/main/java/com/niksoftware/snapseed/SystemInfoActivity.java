package com.niksoftware.snapseed;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.os.Bundle;
import android.os.Debug;
import android.widget.TextView;
import com.niksoftware.snapseed.util.FileHelper;
import java.io.IOException;
import java.io.InputStream;

public class SystemInfoActivity extends Activity {
    TextView _textViewCpuValue;
    TextView _textViewFreeFlashMemValue;
    TextView _textViewGpuValue;
    TextView _textViewSmallHeapSizeValue;

    public void onCreate(Bundle savedInstanceState) {
        String glValues;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sys_infos);
        this._textViewCpuValue = (TextView) findViewById(R.id.textView_cpu_value);
        this._textViewGpuValue = (TextView) findViewById(R.id.textView_gpu_value);
        this._textViewSmallHeapSizeValue = (TextView) findViewById(R.id.textView_smallHeapSize_value);
        this._textViewFreeFlashMemValue = (TextView) findViewById(R.id.textView_freeFlashMem_Value);
        readCpuInfo();
        readMemoryInfo();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            StringBuilder glExtStringBuilder = new StringBuilder();
            appendKeyInfo(glExtStringBuilder, extras, "GL_VENDOR");
            appendKeyInfo(glExtStringBuilder, extras, "GL_RENDERER");
            appendKeyInfo(glExtStringBuilder, extras, "GL_VERSION");
            appendKeyInfo(glExtStringBuilder, extras, "GL_EXTENSIONS");
            glValues = glExtStringBuilder.toString();
        } else {
            glValues = "Unknown\n\n";
        }
        this._textViewGpuValue.setText(glValues);
        this._textViewFreeFlashMemValue.setText(String.format("Readable: %b\nWritable: %b\n\n", new Object[]{Boolean.valueOf(FileHelper.isSDCardReadable()), Boolean.valueOf(FileHelper.isSDCardWritable())}));
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void appendKeyInfo(StringBuilder infoStringBuilder, Bundle bundle, String key) {
        if (infoStringBuilder != null && bundle != null && bundle.containsKey(key)) {
            infoStringBuilder.append(key).append(": ").append(bundle.getString(key)).append("\n");
        }
    }

    public void readMemoryInfo() {
        ActivityManager activityManager = (ActivityManager) getSystemService("activity");
        MemoryInfo memoryInfo = new MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        StringBuilder s = new StringBuilder();
        s.append("MemoryInfo.availMem: ").append(memoryInfo.availMem / ((long) 1048576)).append("\n");
        s.append("MemoryInfo.lowMemory: ").append(memoryInfo.lowMemory).append("\n");
        s.append("MemoryInfo.threshold: ").append(memoryInfo.threshold / ((long) 1048576)).append("\n\n");
        s.append("Debug.getNativeHeapFreeSize: ").append(Debug.getNativeHeapFreeSize() / ((long) 1048576)).append("\n");
        s.append("Debug.getNativeHeapAllocatedSize: ").append(Debug.getNativeHeapAllocatedSize() / ((long) 1048576)).append("\n");
        s.append("Debug.getNativeHeapSize: ").append(Debug.getNativeHeapSize() / ((long) 1048576)).append("\n\n");
        s.append("ActivityManager.getMemoryClass: ").append(activityManager.getMemoryClass()).append("\n");
        s.append("ActivityManager.getLargeMemoryClass: ").append(activityManager.getLargeMemoryClass()).append("\n");
        this._textViewSmallHeapSizeValue.setText(s.toString());
    }

    private void readCpuInfo() {
        String result = "";
        try {
            InputStream in = new ProcessBuilder(new String[]{"/system/bin/cat", "/proc/cpuinfo"}).start().getInputStream();
            byte[] re = new byte[1024];
            StringBuilder buf = new StringBuilder();
            while (in.read(re) != -1) {
                buf.append(new String(re));
            }
            in.close();
            result = buf.toString();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        this._textViewCpuValue.setText(result.trim() + "\n");
    }
}

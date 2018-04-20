package com.niksoftware.snapseed.core;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.niksoftware.snapseed.MainActivity;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.DecimalFormat;

public class SnapseedAppDelegate implements UncaughtExceptionHandler {
    private static final String LOG_TAG = "SnapseedAppDelegate";
    private static SnapseedAppDelegate s_instance;
    final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            int total = msg.getData().getInt("total");
            ProcessDialogFragment fg = ProcessDialogFragment._fg;
            if (fg != null) {
                fg.setProgress(total);
            }
        }
    };

    public static class ProcessDialogFragment extends DialogFragment {
        static final /* synthetic */ boolean $assertionsDisabled = (!SnapseedAppDelegate.class.desiredAssertionStatus());
        private static ProcessDialogFragment _fg = null;
        private static String s_msg;
        private static int s_style;
        private String _msg = s_msg;
        private ProgressDialog _progDialog;
        private int _style = s_style;

        public ProcessDialogFragment() {
            Bundle bundle = new Bundle();
            bundle.putString("msg", this._msg);
            bundle.putInt("style", this._style);
            setArguments(bundle);
            _fg = this;
        }

        public static ProcessDialogFragment newInstance(String msg, int style) {
            s_style = style;
            s_msg = msg;
            return new ProcessDialogFragment();
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setCancelable(false);
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            ProgressDialog progDialog = new ProgressDialog(MainActivity.getMainActivity());
            progDialog.setProgressStyle(this._style);
            progDialog.setMessage(this._msg);
            switch (this._style) {
                case 0:
                    progDialog.setIndeterminate(true);
                    break;
                case 1:
                    progDialog.setMax(100);
                    break;
            }
            this._progDialog = progDialog;
            return progDialog;
        }

        public Runnable dismissFunction() {
            return new Runnable() {
                public void run() {
                    if (ProcessDialogFragment.this.isResumed()) {
                        ProcessDialogFragment.this.dismiss();
                    } else {
                        ProcessDialogFragment.this.dismissAllowingStateLoss();
                    }
                }
            };
        }

        public void setProgress(int total) {
            if (!$assertionsDisabled && this._style != 1) {
                throw new AssertionError();
            } else if (this._progDialog != null) {
                this._progDialog.setProgress(total);
            }
        }
    }

    private SnapseedAppDelegate() {
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    public static SnapseedAppDelegate getInstance() {
        if (s_instance == null) {
            s_instance = new SnapseedAppDelegate();
        }
        return s_instance;
    }

    public static void destroyInstance() {
        s_instance = null;
    }

    public void uncaughtException(Thread thread, Throwable ex) {
        Log.e(LOG_TAG, "uncaughtException()");
        ex.printStackTrace();
        if (ex.toString() != null) {
            Log.e(LOG_TAG, ex.toString());
        }
        if (ex.getMessage() != null) {
            Log.e(LOG_TAG, ex.getMessage());
        }
        if (ex.getLocalizedMessage() != null) {
            Log.e(LOG_TAG, ex.getLocalizedMessage());
        }
    }

    public void progressStart(int msg) {
        progressStart(MainActivity.getMainActivity().getString(msg));
    }

    private void progressStart(final String msg) {
        MainActivity.getMainActivity().runOnUiThread(new Runnable() {
            public void run() {
                ProcessDialogFragment.newInstance(msg, 1).show(MainActivity.getMainActivity().getFragmentManager().beginTransaction(), "PROCESS_DIALOG_FRAGMENT_TAG");
            }
        });
    }

    public void progressStartIndeterminate(int msg) {
        progressStartIndeterminate(MainActivity.getMainActivity().getString(msg));
    }

    private void progressStartIndeterminate(final String msg) {
        MainActivity.getMainActivity().runOnUiThread(new Runnable() {
            public void run() {
                ProcessDialogFragment.newInstance(msg, 0).show(MainActivity.getMainActivity().getFragmentManager().beginTransaction(), "PROCESS_DIALOG_FRAGMENT_IND_TAG");
            }
        });
    }

    public void progressEnd() {
        if (ProcessDialogFragment._fg != null) {
            ProcessDialogFragment fg = ProcessDialogFragment._fg;
            ProcessDialogFragment._fg = null;
            MainActivity.getMainActivity().runOnUiThread(fg.dismissFunction());
        }
    }

    public void progressSetValue(int v) {
        Message msg = this.mHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putInt("total", v);
        msg.setData(bundle);
        this.mHandler.sendMessage(msg);
    }

    public static void logHeap() {
        Double allocated = Double.valueOf(((double) Debug.getNativeHeapAllocatedSize()) / 1048576.0d);
        Double available = Double.valueOf(((double) Debug.getNativeHeapSize()) / 1048576.0d);
        Double free = Double.valueOf(((double) Debug.getNativeHeapFreeSize()) / 1048576.0d);
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        Log.e("HEAP", "debug.heap native: allocated " + df.format(allocated) + "MB of " + df.format(available) + "MB (" + df.format(free) + "MB free) ");
        Log.e("HEAP", "debug.memory: allocated: " + df.format(((double) Runtime.getRuntime().totalMemory()) / 1048576.0d) + "MB of " + df.format(((double) Runtime.getRuntime().maxMemory()) / 1048576.0d) + "MB (" + df.format(((double) Runtime.getRuntime().freeMemory()) / 1048576.0d) + "MB free)");
    }
}

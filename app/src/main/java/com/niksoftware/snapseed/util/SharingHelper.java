package com.niksoftware.snapseed.util;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ResolveInfo.DisplayNameComparator;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

public class SharingHelper {
    private static final String GOOGLE_PLUS_PACKAGE_NAME = "com.google.android.apps.plus";

    private static class ShareIntentAdapter extends ArrayAdapter<ResolveInfo> {
        private int _appIconSize = this._parentActivity.getResources().getDimensionPixelSize(17104896);
        private PackageManager _packageManager;
        private Activity _parentActivity;

        public ShareIntentAdapter(Activity parentActivity, List<ResolveInfo> activityList, PackageManager packageManager) {
            super(parentActivity, R.layout.activity_list_item, activityList);
            this._packageManager = packageManager;
            this._parentActivity = parentActivity;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = this._parentActivity.getLayoutInflater().inflate(R.layout.activity_list_item, parent, false);
            }
            ResolveInfo resolveInfo = (ResolveInfo) getItem(position);
            ((TextView) convertView).setText(resolveInfo.loadLabel(this._packageManager));
            Drawable icon = resolveInfo.loadIcon(this._packageManager);
            if (icon != null) {
                icon.setBounds(0, 0, this._appIconSize, this._appIconSize);
                ((TextView) convertView).setCompoundDrawables(icon, null, null, null);
            }
            return convertView;
        }
    }

    private SharingHelper() {
    }

    public static void showShareDialog(final Activity parentActivity, final Uri imageUri, boolean shareToGooglePlus) {
        if (shareToGooglePlus) {
            openShareActivity(parentActivity, imageUri, GOOGLE_PLUS_PACKAGE_NAME);
            return;
        }
        PackageManager packageManager = parentActivity.getPackageManager();
        Intent shareIntent = new Intent("android.intent.action.SEND");
        shareIntent.setType("image/*");
        final List<ResolveInfo> activityList = packageManager.queryIntentActivities(shareIntent, 0);
        ResolveInfo snapseedResolveInfo = null;
        String snapseedPackageName = parentActivity.getApplicationInfo().packageName;
        for (ResolveInfo resolveInfo : activityList) {
            if (resolveInfo.activityInfo.packageName.equals(snapseedPackageName)) {
                snapseedResolveInfo = resolveInfo;
                break;
            }
        }
        activityList.remove(snapseedResolveInfo);
        Collections.sort(activityList, new DisplayNameComparator(packageManager));
        Builder dialogBuilder = new Builder(parentActivity);
        dialogBuilder.setTitle(R.string.share_btn);
        dialogBuilder.setAdapter(new ShareIntentAdapter(parentActivity, activityList, packageManager), new OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int which) {
                SharingHelper.openShareActivity(parentActivity, imageUri, ((ResolveInfo) activityList.get(which)).activityInfo.packageName);
            }
        });
        dialogBuilder.show();
    }

    private static void openShareActivity(Activity parentActivity, Uri imageUri, String packageName) {
        Resources resources = parentActivity.getResources();
        TrackerData.getInstance().sendDataImagesShared(packageName == GOOGLE_PLUS_PACKAGE_NAME);
        Intent shareIntent = new Intent("android.intent.action.SEND");
        shareIntent.setPackage(packageName);
        shareIntent.setType("image/*");
        if (!packageName.equals(GOOGLE_PLUS_PACKAGE_NAME)) {
            shareIntent.putExtra("android.intent.extra.SUBJECT", resources.getString(R.string.intent_subject));
            shareIntent.putExtra("android.intent.extra.TEXT", resources.getString(R.string.intent_subject));
        }
        shareIntent.putExtra("android.intent.extra.STREAM", imageUri);
        boolean startActivitySucceeded = true;
        try {
            parentActivity.startActivity(shareIntent);
        } catch (ActivityNotFoundException e) {
            startActivitySucceeded = false;
        }
        if (!startActivitySucceeded) {
            try {
                Intent playStoreIntent = new Intent("android.intent.action.VIEW");
                playStoreIntent.setData(Uri.parse("market://details?id=" + packageName));
                parentActivity.startActivity(playStoreIntent);
            } catch (Exception e2) {
            }
        }
    }
}

package com.niksoftware.snapseed;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;

@SuppressLint({"ValidFragment"})
public class SettingsActivity extends Activity {
    private static final String BUILD_VERSION_PREFERENCE = "build_version";

    private class SettingsFragment extends PreferenceFragment {
        private SettingsFragment() {
        }

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
            PreferenceGroup buildVersion = (PreferenceGroup) findPreference(SettingsActivity.BUILD_VERSION_PREFERENCE);
            PackageManager packageManager = SettingsActivity.this.getPackageManager();
            if (buildVersion != null && packageManager != null) {
                try {
                    buildVersion.setSummary(packageManager.getPackageInfo(SettingsActivity.this.getPackageName(), 0).versionName);
                } catch (NameNotFoundException e) {
                    buildVersion.setSummary("");
                }
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(16908290, new SettingsFragment()).commit();
    }
}

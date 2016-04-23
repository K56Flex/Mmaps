/*
 * Copyright 2016 Shen Zhang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dg.shenm233.mmaps.ui.setting;

import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.Toast;

import java.io.File;

import dg.shenm233.mmaps.BuildConfig;
import dg.shenm233.mmaps.CrashHandler;
import dg.shenm233.mmaps.MainApplication;
import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.ui.LicenseActivity;
import dg.shenm233.mmaps.util.CommonUtils;

public class GeneralSettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceClickListener {
    private static final String source_code = "https://github.com/shenm233/Mmaps";

    private static final String navi_settings = "navi_settings";
    private static final String version = "version";
    private static final String license = "license";
    private static final String feedback = "feedback";
    private static final String open_source = "open_source";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_general);
        findPreference(navi_settings).setOnPreferenceClickListener(this);
        findPreference(version).setSummary(BuildConfig.VERSION_NAME);
        findPreference(license).setOnPreferenceClickListener(this);
        findPreference(feedback).setOnPreferenceClickListener(this);

        Preference open = findPreference(open_source);
        open.setOnPreferenceClickListener(this);
        open.setSummary(source_code);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();

        if (navi_settings.equals(key)) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.main_content, new NaviSettingsFragment(), NaviSettingsFragment.class.getSimpleName());
            ft.addToBackStack(null);
            ft.commit();
            return true;
        } else if (license.equals(key)) {
            Intent intent = new Intent(getActivity(), LicenseActivity.class);
            startActivity(intent);
        } else if (feedback.equals(key)) {
            sendFeedback();
        } else if (open_source.equals(key)) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(source_code));
            startActivity(intent);
        }

        return false;
    }

    private void sendFeedback() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"darkgenlotus@gmail.com"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "MMaps 应用反馈");

        String last_log = CommonUtils.getStringFromFile(CrashHandler.getLastLogPath());
        if (!CommonUtils.isStringEmpty(last_log)) {
            last_log = last_log.trim();
            File attachment = new File(MainApplication.getCrashReportsPath() + "/" + last_log);
            if (attachment.exists() && attachment.canRead()) {
                intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(attachment));
            }
        }
        try {
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), R.string.not_installed_email, Toast.LENGTH_SHORT).show();
        }
    }
}

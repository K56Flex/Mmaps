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
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import dg.shenm233.mmaps.BuildConfig;
import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.ui.LicenseActivity;

public class GeneralSettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceClickListener {
    private static final String navi_settings = "navi_settings";
    private static final String version = "version";
    private static final String license = "license";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_general);
        findPreference(navi_settings).setOnPreferenceClickListener(this);
        findPreference(version).setSummary(BuildConfig.VERSION_NAME);
        findPreference(license).setOnPreferenceClickListener(this);
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
        }

        return false;
    }
}

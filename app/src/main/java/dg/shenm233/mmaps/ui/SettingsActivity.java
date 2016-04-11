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

package dg.shenm233.mmaps.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;

import dg.shenm233.mmaps.R;
import dg.shenm233.mmaps.ui.maps.MapsFragment;
import dg.shenm233.mmaps.ui.setting.GeneralSettingsFragment;
import dg.shenm233.mmaps.ui.setting.NaviSettingsFragment;

public class SettingsActivity extends Activity {
    public static final String FRAGMENT_TO_START = "start_fragment";

    public static final String GENERAL_FRAGMENT = "general_fragment";
    public static final String NAVI_FRAGMENT = "navi_fragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Intent intent = getIntent();
        String fragmentToStart = intent.getStringExtra(FRAGMENT_TO_START);

        Fragment fragment;
        if (NAVI_FRAGMENT.equals(fragmentToStart)) {
            fragment = new NaviSettingsFragment();
        } else {
            fragment = new GeneralSettingsFragment();
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.main_content, fragment, MapsFragment.class.getName());
        ft.commit();
    }

}

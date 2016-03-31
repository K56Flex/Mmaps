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

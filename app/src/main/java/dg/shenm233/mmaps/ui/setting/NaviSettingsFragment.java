package dg.shenm233.mmaps.ui.setting;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import dg.shenm233.mmaps.R;

public class NaviSettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_navi);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return false;
    }
}

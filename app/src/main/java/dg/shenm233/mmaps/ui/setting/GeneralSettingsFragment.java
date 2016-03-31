package dg.shenm233.mmaps.ui.setting;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import dg.shenm233.mmaps.BuildConfig;
import dg.shenm233.mmaps.R;

public class GeneralSettingsFragment extends PreferenceFragment
        implements Preference.OnPreferenceClickListener {
    private static final String navi_settings = "navi_settings";
    private static final String version = "version";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference_general);
        findPreference(navi_settings).setOnPreferenceClickListener(this);
        findPreference(version).setSummary(BuildConfig.VERSION_NAME);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (navi_settings.equals(preference.getKey())) {
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.main_content, new NaviSettingsFragment(), NaviSettingsFragment.class.getSimpleName());
            ft.addToBackStack(null);
            ft.commit();
            return true;
        }

        return false;
    }
}

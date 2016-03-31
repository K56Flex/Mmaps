package dg.shenm233.mmaps.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class NaviSettings {
    private static final String KEY_PREF_KEEP_SCREEN_BRIGHT = "navi_keep_screen_bright";
    private static final String KEY_PREF_NIGHT_MODE = "navi_night_mode";
    private static final String KEY_PREF_RECALC_FOR_JAM = "navi_reclac_for_jam";
    private static final String KEY_PREF_RECALC_FOR_YAW = "navi_recalc_for_yaw";

    private static SharedPreferences mPreferences;

    public static void init(Context context) {
        if (mPreferences == null) {
            mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        }
    }

    public static void destroy() {
        mPreferences = null;
    }

    public static boolean getScreenBright() {
        return mPreferences != null && mPreferences.getBoolean(KEY_PREF_KEEP_SCREEN_BRIGHT, false);
    }

    public static void setScreenBright(boolean enable) {
        if (mPreferences != null) {
            mPreferences.edit().putBoolean(KEY_PREF_KEEP_SCREEN_BRIGHT, enable).apply();
        }
    }

    public static boolean getNightMode() {
        return mPreferences != null && mPreferences.getBoolean(KEY_PREF_NIGHT_MODE, false);
    }

    public static void setNightMode(boolean enable) {
        if (mPreferences != null) {
            mPreferences.edit().putBoolean(KEY_PREF_NIGHT_MODE, enable).apply();
        }
    }

    public static boolean getRecalcForJam() {
        return mPreferences != null && mPreferences.getBoolean(KEY_PREF_RECALC_FOR_JAM, false);
    }

    public static void setRecalcForJam(boolean enable) {
        if (mPreferences != null) {
            mPreferences.edit().putBoolean(KEY_PREF_RECALC_FOR_JAM, enable).apply();
        }
    }

    public static boolean getRecalcForYaw() {
        return mPreferences != null && mPreferences.getBoolean(KEY_PREF_RECALC_FOR_YAW, false);
    }

    public static void setRecalcForYaw(boolean enable) {
        if (mPreferences != null) {
            mPreferences.edit().putBoolean(KEY_PREF_RECALC_FOR_YAW, enable).apply();
        }
    }
}

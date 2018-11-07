package eu.miaounyan.isthereanynetwork.controller;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import eu.miaounyan.isthereanynetwork.R;

public class PreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String VALUE_PREF_ALARM_RECEIVE = "AlarmReceiverPreference";
    private static final String KEY_PREF_ALARM_RECEIVER = "main";
    private SharedPreferences prefs;
    private Activity activity;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setPreferencesFromResource(R.xml.preferences, s);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    }

    /*// Alternative
    @Override
    public void onResume() {
        super.onResume();

        // Quick Order enable
        final SwitchPreferenceCompat alarmReceiverSwitchPreference = (SwitchPreferenceCompat) findPreference("main");
        if (alarmReceiverSwitchPreference != null) {
            alarmReceiverSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                prefs.edit().putBoolean(KEY_PREF_ALARM_RECEIVER, (Boolean) newValue).apply();
                return true;
            });
        }
    }
    */

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_PREF_ALARM_RECEIVER)) {
            prefs.edit().putBoolean(KEY_PREF_ALARM_RECEIVER, prefs.getBoolean(key, true)).apply();
        }
    }
}

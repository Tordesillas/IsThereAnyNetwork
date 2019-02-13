package eu.miaounyan.isthereanynetwork.controller;

import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import eu.miaounyan.isthereanynetwork.R;

import static eu.miaounyan.isthereanynetwork.utils.PreferencesUtilities.KEY_PREF_ALARM_RECEIVER;
import static eu.miaounyan.isthereanynetwork.utils.PreferencesUtilities.KEY_PREF_ALARM_RECEIVER_CACHE;
import static eu.miaounyan.isthereanynetwork.utils.PreferencesUtilities.KEY_PREF_ALARM_RECEIVER_INTERVAL;
import static eu.miaounyan.isthereanynetwork.utils.PreferencesUtilities.getAlarmReceiverInterval;
import static eu.miaounyan.isthereanynetwork.utils.ServiceUtilities.CACHE_INTERVAL;
import static eu.miaounyan.isthereanynetwork.utils.ServiceUtilities.getAlarmPendingIntent;
import static eu.miaounyan.isthereanynetwork.utils.ServiceUtilities.getCacheAlarmPendingIntent;
import static eu.miaounyan.isthereanynetwork.utils.ServiceUtilities.setAlarmPendingIntent;
import static eu.miaounyan.isthereanynetwork.utils.ServiceUtilities.setCacheAlarmPendingIntent;

public class PreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences prefs;
    private AlarmManager manager;

    protected static PreferenceFragment newInstance() {
        return new PreferenceFragment();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        setPreferencesFromResource(R.xml.preferences, s);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(getActivity(), R.xml.preferences, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        prefs.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        boolean booleanValue;

        switch (key) {
            case KEY_PREF_ALARM_RECEIVER:
                booleanValue = prefs.getBoolean(key, true);
                prefs.edit().putBoolean(key, booleanValue).apply();

                if (booleanValue) {
                    startAlarm();
                } else {
                    stopAlarm();
                }
                break;
            case KEY_PREF_ALARM_RECEIVER_INTERVAL:
                // As the preference edit text will always be a string, we put this preference value as a string.
                String stringValue = prefs.getString(key, "5");
                prefs.edit().putString(key, stringValue).apply();
                int integerValue = getAlarmReceiverInterval(getActivity());

                // Safe operations
                prefs.edit().putString(key, Integer.toString(integerValue)).apply();
                EditTextPreference textPreference = (EditTextPreference) getPreferenceManager().findPreference(key);
                textPreference.setText(Integer.toString(integerValue));

                setAlarmInterval(integerValue * 1000 * 60);
                break;
            case KEY_PREF_ALARM_RECEIVER_CACHE:
                booleanValue = prefs.getBoolean(key, true);
                prefs.edit().putBoolean(key, booleanValue).apply();

                if (booleanValue) {
                    startCache();
                } else {
                    stopCache();
                }
                break;
        }
    }

    /**
     * Called whenever the alarm receiver preference is set to enabled.
     */
    private void startAlarm() {
        setAlarmPendingIntent(getActivity());

        manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                getAlarmReceiverInterval(getActivity()), getAlarmPendingIntent());

        Toast.makeText(getActivity(), "Alarm Set", Toast.LENGTH_SHORT).show();
        Log.d(this.getClass().getName(), "Alarm Set");
    }

    /**
     * Stops the current alarm pending intent (unique for the whole application).
     */
    private void stopAlarm() {
        manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        manager.cancel(getAlarmPendingIntent());

        Toast.makeText(getActivity(), "Alarm Stopped", Toast.LENGTH_SHORT).show();
        Log.d(this.getClass().getName(), "Alarm Stopped");
    }

    /**
     * Sets the current alarm pending intent repeating interval.
     * This interval range will always be between 1 and 59 according to Android Alarm API and cache
     * policy of the application.
     *
     * @param intervalMillis the current alarm pending intent repeating interval in milliseconds.
     */
    private void setAlarmInterval(int intervalMillis) {
        manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalMillis, getAlarmPendingIntent());

        Toast.makeText(getActivity(), "Alarm interval set to " + intervalMillis / (1000 * 60) + "min", Toast.LENGTH_SHORT).show();
        Log.d(this.getClass().getName(), "Alarm interval set to " + intervalMillis / (1000 * 60) + "min");
    }

    private void startCache() {
        setCacheAlarmPendingIntent(getActivity());

        manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                CACHE_INTERVAL * 1000 * 60, getCacheAlarmPendingIntent());

        Toast.makeText(getActivity(), "Alarm receiver cache enabled", Toast.LENGTH_SHORT).show();
        Log.d(this.getClass().getName(), "Alarm receiver cache enabled");
    }

    private void stopCache() {
        manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        manager.cancel(getCacheAlarmPendingIntent());
        // TODO Either flush data when cancelling to prevent losing data when application is closed or save them (internal or external storage)

        Toast.makeText(getActivity(), "Alarm receiver cache disabled", Toast.LENGTH_SHORT).show();
        Log.d(this.getClass().getName(), "Alarm receiver cache disabled");
    }
}

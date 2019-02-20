package eu.miaounyan.isthereanynetwork.controller;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import eu.miaounyan.isthereanynetwork.R;
import eu.miaounyan.isthereanynetwork.service.background.AlarmSetter;

import static eu.miaounyan.isthereanynetwork.utils.PreferencesUtilities.KEY_PREF_ALARM_RECEIVER;
import static eu.miaounyan.isthereanynetwork.utils.PreferencesUtilities.KEY_PREF_ALARM_RECEIVER_CACHE;
import static eu.miaounyan.isthereanynetwork.utils.PreferencesUtilities.KEY_PREF_ALARM_RECEIVER_INTERVAL;
import static eu.miaounyan.isthereanynetwork.utils.PreferencesUtilities.getAlarmReceiverInterval;


public class PreferenceFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedPreferences prefs;
    private AlarmSetter alarmSetter;

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

        alarmSetter = new AlarmSetter(this.getContext());
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
                    alarmSetter.startAlarm();
                } else {
                    alarmSetter.stopAlarm();
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

                alarmSetter.stopAlarm();
                alarmSetter.startAlarm();
                break;
            case KEY_PREF_ALARM_RECEIVER_CACHE:
                booleanValue = prefs.getBoolean(key, true);
                prefs.edit().putBoolean(key, booleanValue).apply();

                if (booleanValue) {
                    alarmSetter.startCache();
                } else {
                    alarmSetter.stopCache();
                }
                break;
        }
    }
}

package eu.miaounyan.isthereanynetwork.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

public final class PreferencesUtilities {

    public static final String KEY_PREF_ALARM_RECEIVER = "alarm_receiver_enable";
    public static final String KEY_PREF_ALARM_RECEIVER_INTERVAL = "alarm_receiver_interval";
    public static final String KEY_PREF_ALARM_RECEIVER_CACHE = "alarm_receiver_cache";

    private PreferencesUtilities() {
    }

    private static int getEditTextPreferenceAsInt(Context context, String key) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String value = prefs.getString(key, null);
        int intValue = -1;

        try {
            intValue = Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
        }

        return intValue;
    }

    /**
     * Gets alarm receiver data sending interval in minute.
     *
     * @param context the context from which retrieving the interval
     * @return the alarm receiver data sending interval in minute. Multiply this value by 1000 to
     * get milliseconds (when passing this value to the setRepeating method for instance)
     */
    public static int getAlarmReceiverInterval(Context context) {
        int interval = getEditTextPreferenceAsInt(context, KEY_PREF_ALARM_RECEIVER_INTERVAL);
        /* Default value is considered as 5 minutes. Highest one as 59 minutes according to
         * cache policy which sends data each one hour.
         */
        return interval < 1 ? 5 : interval > 59 ? 59 : interval;
    }
}

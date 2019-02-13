package eu.miaounyan.isthereanynetwork.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import eu.miaounyan.isthereanynetwork.service.background.AlarmReceiver;
import eu.miaounyan.isthereanynetwork.service.background.AlarmReceiverCache;

public final class ServiceUtilities {

    /**
     * Cache interval in minutes
     */
    public static final int CACHE_INTERVAL = 60;
    /**
     * This pending intent does not need to be saved when application is killed and is unique for
     * the whole application. Thus for simplicity purpose (avoid bundle manipulation and preference
     * fragment instance saving), we use a static object provided with control setter and getter.
     */
    private static PendingIntent alarmPendingIntent;
    /**
     * This pending intent has the same properties as {@see alarmPendingIntent}, except it does not
     * perform the same job.
     */
    private static PendingIntent cacheAlarmPendingIntent;

    private ServiceUtilities() {
    }

    /**
     * Gets the unique application alarm pending intent.
     * The returned object will never be null as the call to this getter is always predated by a
     * call to {@see setAlarmPendingIntent}.
     *
     * @return the unique application alarm pending intent
     */
    public static PendingIntent getAlarmPendingIntent() {
        return alarmPendingIntent;
    }

    /**
     * Sets the application alarm pending intent for one specific alarm receiver.
     *
     * @param context the context from which the alarm pending intent is set
     */
    public static void setAlarmPendingIntent(Context context) {
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(context, 100, alarmIntent, 0);
    }

    /**
     * Gets the unique application cache alarm pending intent.
     * The returned object will never be null as the call to this getter is always predated by a
     * call to {@see setCacheAlarmPendingIntent}.
     *
     * @return the unique application alarm pending intent
     */
    public static PendingIntent getCacheAlarmPendingIntent() {
        return cacheAlarmPendingIntent;
    }

    /**
     * Sets the application cache alarm pending intent for one specific alarm receiver.
     *
     * @param context the context from which the cache alarm pending intent is set
     */
    public static void setCacheAlarmPendingIntent(Context context) {
        Intent alarmIntent = new Intent(context, AlarmReceiverCache.class);
        cacheAlarmPendingIntent = PendingIntent.getBroadcast(context, 200, alarmIntent, 0);
    }
}

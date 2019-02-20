package eu.miaounyan.isthereanynetwork.service.background

import android.app.AlarmManager
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import android.widget.Toast
import eu.miaounyan.isthereanynetwork.utils.PreferencesUtilities
import eu.miaounyan.isthereanynetwork.utils.ServiceUtilities

class AlarmSetter(private val context: Context) {
    private val alarmReceiver = AlarmReceiver()
    private val alarmReceiverCache = AlarmReceiverCache()
    private val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private var isAlarmStarted = false
    private var isCacheStarted = false

    private fun registerAlarm() {
        Log.d(this.javaClass.name, "registerAlarm")

        // Apparently needs to be registered since Android 7.0
        val intentFilter = IntentFilter("eu.miaounyan.isthereanynetwork.service.background.AlarmReceiver")
        context.registerReceiver(alarmReceiver, intentFilter)
    }
    private fun unregisterAlarm() {
        Log.d(this.javaClass.name, "unregisterAlarm")

        context.unregisterReceiver(alarmReceiver)
    }

    private fun registerAlarmCache() {
        Log.d(this.javaClass.name, "registerAlarmCache")

        // Apparently needs to be registered since Android 7.0
        val intentFilter = IntentFilter("eu.miaounyan.isthereanynetwork.service.background.AlarmReceiverCache")
        context.registerReceiver(alarmReceiverCache, intentFilter)
    }

    private fun unregisterAlarmCache() {
        Log.d(this.javaClass.name, "unregisterAlarmCache")

        context.unregisterReceiver(alarmReceiverCache)
    }

    /**
     * Called whenever the alarm receiver preference is set to enabled.
     */
    fun startAlarm() {
        // don't try to start again if it's already set, should be linked to intent but whatever…
        if (isAlarmStarted)
            return

        isAlarmStarted = true
        ServiceUtilities.setAlarmPendingIntent(context)

        //registerAlarm()

        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                PreferencesUtilities.getAlarmReceiverInterval(context).toLong(), ServiceUtilities.getAlarmPendingIntent())

        Toast.makeText(context, "Alarm Set", Toast.LENGTH_SHORT).show()
        Log.d(this.javaClass.name, "Alarm Set")
    }

    /**
     * Stops the current alarm pending intent (unique for the whole application).
     */
    fun stopAlarm() {
        isAlarmStarted = false
        manager.cancel(ServiceUtilities.getAlarmPendingIntent())

        //unregisterAlarm()

        Toast.makeText(context, "Alarm Stopped", Toast.LENGTH_SHORT).show()
        Log.d(this.javaClass.name, "Alarm Stopped")
    }

    fun startCache() {
        // don't try to start again if it's already set, should be linked to intent but whatever…
        if (isCacheStarted)
            return

        isCacheStarted = true
        ServiceUtilities.setCacheAlarmPendingIntent(context)

        //registerAlarmCache()

        manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
                (ServiceUtilities.CACHE_INTERVAL * 1000 * 60).toLong(), ServiceUtilities.getCacheAlarmPendingIntent())

        Toast.makeText(context, "Alarm receiver cache enabled", Toast.LENGTH_SHORT).show()
        Log.d(this.javaClass.name, "Alarm receiver cache enabled")
    }

    fun stopCache() {
        isCacheStarted = false
        manager.cancel(ServiceUtilities.getCacheAlarmPendingIntent())

        //unregisterAlarmCache()

        Toast.makeText(context, "Alarm receiver cache disabled", Toast.LENGTH_SHORT).show()
        Log.d(this.javaClass.name, "Alarm receiver cache disabled")
    }
}
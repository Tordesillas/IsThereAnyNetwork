package eu.miaounyan.isthereanynetwork.service.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.IsThereAnyNetwork
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.IsThereAnyNetworkService
import eu.miaounyan.isthereanynetwork.model.NetworkState
import eu.miaounyan.isthereanynetwork.model.PendingNetworkStates
import eu.miaounyan.isthereanynetwork.service.location.GPSTracker
import eu.miaounyan.isthereanynetwork.service.telephony.Network
import eu.miaounyan.isthereanynetwork.utils.DataUtilities.getCurrentTimeDate
import eu.miaounyan.isthereanynetwork.utils.PreferencesUtilities.KEY_PREF_ALARM_RECEIVER_CACHE
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AlarmReceiver(val isThereAnyNetwork: IsThereAnyNetwork = IsThereAnyNetwork(),
                    val isThereAnyNetworkService: IsThereAnyNetworkService = isThereAnyNetwork.connect()) : BroadcastReceiver() {

    private val pendingNetworkStates = PendingNetworkStates()

    override fun onReceive(context: Context?, intent: Intent?) {
        Toast.makeText(context, "Sending network state...", Toast.LENGTH_LONG).show();

        val network = context?.run { getSystemService(Context.TELEPHONY_SERVICE)?.let { it as? TelephonyManager }?.let { Network(it, this) } }

        if (network == null) {
            Log.e(javaClass.name, "null context or telephonyManager")
            return
        }

        val gpsTracker = GPSTracker(context)
        if (!gpsTracker.isConsistent) {
            Log.w(javaClass.name, "GPSTracker not consistent")
            return
        }

        // after this synchronous call, telephonyManager doesn't listen anymore
        network.once(context) {
            val networkState = NetworkState(gpsTracker.latitude, gpsTracker.longitude,
                    network.signalStrength, network.operator, getCurrentTimeDate(), network.type)

            val prefs = PreferenceManager.getDefaultSharedPreferences(context)

            if (prefs.getBoolean(KEY_PREF_ALARM_RECEIVER_CACHE, true)) {
                pendingNetworkStates.add(context, networkState)
            } else {
                val alarmServiceIntent = Intent(context, AlarmService::class.java)
                alarmServiceIntent.putExtra("networkStates", arrayOf(networkState))

                context.startService(alarmServiceIntent)
            }
        }
    }
}
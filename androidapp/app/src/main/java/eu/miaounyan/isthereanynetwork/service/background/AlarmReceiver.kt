package eu.miaounyan.isthereanynetwork.service.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import eu.miaounyan.isthereanynetwork.model.IsThereAnyNetworkData
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.IsThereAnyNetwork
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.IsThereAnyNetworkService
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.NetworkState
import eu.miaounyan.isthereanynetwork.service.location.GPSTracker
import eu.miaounyan.isthereanynetwork.utils.DataUtilities.checkDataConsistency
import eu.miaounyan.isthereanynetwork.utils.DataUtilities.getCurrentTimeDate
import eu.miaounyan.isthereanynetwork.utils.PreferencesUtilities.KEY_PREF_ALARM_RECEIVER_CACHE
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AlarmReceiver(val isThereAnyNetwork: IsThereAnyNetwork = IsThereAnyNetwork(),
                    val isThereAnyNetworkService: IsThereAnyNetworkService = isThereAnyNetwork.connect()) : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val telephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?

        if (telephonyManager == null) {
            Log.e(javaClass.name, "null context or telephonyManager")
            return
        }

        val network = eu.miaounyan.isthereanynetwork.service.telephony.Network(telephonyManager, context!!)
        val gpsTracker = GPSTracker(context)

        if (checkDataConsistency(gpsTracker, network)) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)

            if (prefs.getBoolean(KEY_PREF_ALARM_RECEIVER_CACHE, true)) {
                AlarmReceiverCache.isThereAnyNetworkData.add(IsThereAnyNetworkData(network.type,
                        network.operator, network.signalStrength, gpsTracker.latitude, gpsTracker.longitude, getCurrentTimeDate()))
            } else {
                // null check, unneeded since above telephonyManager does it already
                context?.let {
                    network.once(it) {
                        // after this synchronous call, telephonyManager doesn't listen anymore
                        isThereAnyNetworkService.sendNetworkState(NetworkState(gpsTracker.latitude, gpsTracker.longitude,
                                network.signalStrength, network.operator, getCurrentTimeDate(), network.type))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe({ r ->
                                    Log.d(this.javaClass.name, "Sent network state")
                                    Toast.makeText(context, "Sent " + r.signalStrength + " at lat=" +
                                            gpsTracker.latitude + ";lon=" + gpsTracker.longitude, Toast.LENGTH_LONG).show()
                                }, { err ->
                                    Log.e(this.javaClass.name, "Error: $err")
                                    Toast.makeText(context, "Error " + err.message, Toast.LENGTH_LONG).show()
                                })
                    }
                }
            }
        }
    }
}
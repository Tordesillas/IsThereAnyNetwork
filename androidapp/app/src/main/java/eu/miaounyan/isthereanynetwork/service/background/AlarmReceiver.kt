package eu.miaounyan.isthereanynetwork.service.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import eu.miaounyan.isthereanynetwork.service.GPSTracker
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.IsThereAnyNetwork
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.IsThereAnyNetworkService
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.NetworkState
import eu.miaounyan.isthereanynetwork.service.telephony.Network
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

class AlarmReceiver(val isThereAnyNetwork: IsThereAnyNetwork = IsThereAnyNetwork(),
                    val isThereAnyNetworkService: IsThereAnyNetworkService = isThereAnyNetwork.connect()) : BroadcastReceiver() {

    private fun getCurrentTimeDate(): String {
        val now = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone("UTC")
        return dateFormat.format(now)
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        val telephonyManager = context?.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?

        if (telephonyManager == null) {
            Log.e(javaClass.name, "null context or telephonyManager")
            return
        }

        val network = eu.miaounyan.isthereanynetwork.service.telephony.Network(telephonyManager)
        val gpsTracker = GPSTracker(context)

        if (checkDataConsistency(gpsTracker, network)) {
            // null check, unneeded since above telephonyManager does it already
            context?.let {
                network.once(it) {
                    // after this synchronous call, telephonyManager doesn't listen anymore
                    isThereAnyNetworkService.sendNetworkState(NetworkState(gpsTracker.getLatitude(), gpsTracker.getLongitude(), network.signalStrength, network.operator, getCurrentTimeDate(), network.type))
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ r ->
                                Log.d(this.javaClass.name, "Sent network state")
                                Toast.makeText(context, "Sent " + r.signalStrength, Toast.LENGTH_LONG).show()
                            }, { err ->
                                Log.e(this.javaClass.name, "Error: $err")
                                Toast.makeText(context, "Error " + err.message, Toast.LENGTH_LONG).show()
                            })
                }
            }
        }
    }

    private fun checkDataConsistency(gpsTracker: GPSTracker, network: Network): Boolean {
        return (-180 <= gpsTracker.getLatitude() && gpsTracker.getLatitude() <= 180) &&
                (-180 <= gpsTracker.getLongitude() && gpsTracker.getLongitude() <= 180) &&
                (gpsTracker.getLatitude().toInt() != 0 && gpsTracker.getLongitude().toInt() != 0) &&
                (-150 <= network.signalStrength && network.signalStrength < -40) &&
                (!"Unknown".equals(network.type))
    }
}
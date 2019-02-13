package eu.miaounyan.isthereanynetwork.service.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import eu.miaounyan.isthereanynetwork.model.IsThereAnyNetworkData
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.IsThereAnyNetwork
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.IsThereAnyNetworkService
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.NetworkState
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class AlarmReceiverCache(private val isThereAnyNetwork: IsThereAnyNetwork = IsThereAnyNetwork(),
                         private val isThereAnyNetworkService: IsThereAnyNetworkService = isThereAnyNetwork.connect())
    : BroadcastReceiver() {

    companion object {
        val isThereAnyNetworkData: MutableList<IsThereAnyNetworkData> = mutableListOf()
    }

    private val isThereAnyNetworkDataRead: List<IsThereAnyNetworkData> = isThereAnyNetworkData

    override fun onReceive(context: Context?, intent: Intent?) {
        for (itanData in isThereAnyNetworkDataRead) {
            isThereAnyNetworkService.sendNetworkState(NetworkState(itanData.lat, itanData.lon,
                    itanData.signalStrength, itanData.operator, itanData.date, itanData.type))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({ r ->
                        Log.d(this.javaClass.name, "Sent network state")
                        Toast.makeText(context, "Sent " + r.signalStrength + " at lat=" +
                                r.latitude + ";lon=" + r.longitude, Toast.LENGTH_LONG).show()
                    }, { err ->
                        Log.e(this.javaClass.name, "Error: $err")
                        Toast.makeText(context, "Error " + err.message, Toast.LENGTH_LONG).show()
                    })
        }
    }
}
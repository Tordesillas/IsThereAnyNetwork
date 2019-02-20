package eu.miaounyan.isthereanynetwork.service.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import eu.miaounyan.isthereanynetwork.model.PendingNetworkStates
import eu.miaounyan.isthereanynetwork.utils.PermittedToast


class AlarmReceiverCache()
    : BroadcastReceiver() {
    private val pendingNetworkStates = PendingNetworkStates()


    override fun onReceive(context: Context?, intent: Intent?) {
        PermittedToast.makeText(context, "Sending network state from cache...", Toast.LENGTH_LONG)?.show();

        val networkStates = pendingNetworkStates.fetch(context)
        pendingNetworkStates.removeAll(context)


        val alarmServiceIntent = Intent(context, AlarmService::class.java)
        alarmServiceIntent.putExtra("networkStates", networkStates.toTypedArray())

        context?.startService(alarmServiceIntent)
    }
}
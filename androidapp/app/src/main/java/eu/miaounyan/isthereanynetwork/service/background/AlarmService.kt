package eu.miaounyan.isthereanynetwork.service.background

import android.app.IntentService
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import eu.miaounyan.isthereanynetwork.model.NetworkState
import eu.miaounyan.isthereanynetwork.model.PendingNetworkStates
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.IsThereAnyNetwork
import eu.miaounyan.isthereanynetwork.service.isthereanynetwork.IsThereAnyNetworkService
import eu.miaounyan.isthereanynetwork.utils.PermittedToast
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class AlarmService : Service() {

    private val isThereAnyNetwork: IsThereAnyNetwork = IsThereAnyNetwork()
    private val isThereAnyNetworkService: IsThereAnyNetworkService = isThereAnyNetwork.connect()
    private val pendingNetworkStates = PendingNetworkStates()
    private val alarmSetter by lazy { AlarmSetter(applicationContext) }

    private var _disposables = CompositeDisposable()

    // should be custom getter but won't compile so use a function
    private fun disposables(): CompositeDisposable {
        if (_disposables.isDisposed)
            _disposables = CompositeDisposable()
        return _disposables
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(javaClass.name, "onHandleIntent")

        val networkStates = intent?.extras?.getParcelableArray("networkStates")
        networkStates?.filterIsInstance<NetworkState>()?.forEach {
            Log.d(javaClass.name, "ns: " + it)
            disposables().add(
                    isThereAnyNetworkService.sendNetworkState(it)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ r ->
                                Log.d(this.javaClass.name, "Sent network state")
                                PermittedToast.makeText(applicationContext, "Sent " + r.signalStrength + " at lat=" +
                                        r.latitude + ";lon=" + r.longitude, Toast.LENGTH_LONG)?.show()
                            }, { err ->
                                Log.e(this.javaClass.name, "Error: $err")
                                PermittedToast.makeText(applicationContext, "Error " + err.message, Toast.LENGTH_LONG)?.show()

                                // error while sending, enqueue again
                                pendingNetworkStates.add(applicationContext, it)
                                // don't care about KEY_PREF_ALARM_RECEIVER_CACHE if we enqueue something due to network error, start cache anyway
                                alarmSetter.startCache()
                            })
            )

        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        disposables().dispose()
    }
}

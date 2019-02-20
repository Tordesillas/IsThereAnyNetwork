package eu.miaounyan.isthereanynetwork.service.telephony

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.telephony.*
import android.util.Log
import eu.miaounyan.isthereanynetwork.service.PermissionManager

class Network(private val telephonyManager: TelephonyManager, context: Context) {

    private val pmanager = PermissionManager()
    private val networkTypeSerializer = NetworkTypeSerializer()
    private var m_signalStrength = 0
    private var m_signalLevel = 0
    private val phoneStateListener = NetworkPhoneStateListener()

    internal inner class NetworkPhoneStateListener : PhoneStateListener() {
        var callback = {}

        override fun onSignalStrengthsChanged(sigStrength: SignalStrength) {
            super.onSignalStrengthsChanged(sigStrength)

            if (telephonyManager.networkType == TelephonyManager.NETWORK_TYPE_LTE) {
                try {
                    m_signalStrength = sigStrength.javaClass.getMethod("getLteRsrp").invoke(sigStrength) as Int
                    Log.d(this.javaClass.name, "getLteRsrp: $m_signalStrength")

                    m_signalLevel = sigStrength.javaClass.getMethod("getLteLevel").invoke(sigStrength) as Int
                    Log.d(this.javaClass.name, "Signal Strength Level: $m_signalLevel")
                } catch (ex: Exception) {
                    Log.v("Error", "Couldn't retrieve either signal strength or signal level - " + ex.message)
                }

                // Alternative
                //signalStrength = getLTESignalStrength(sigStrength);
                // There's also getLTEdBm which is a getter of mLteRsrp
                //networkData.setText(m_signalStrength.toString() + " dBm\n\nSignal Level: " + m_signalLevel)
            } else {
                //assume that everything that's not LTE is akin to GSM, too much cases to take into account otherwise
                //if (telephonyManager.networkType == TelephonyManager.NETWORK_TYPE_CDMA || telephonyManager.networkType == TelephonyManager.NETWORK_TYPE_GSM) {
                m_signalStrength = 2 * sigStrength.gsmSignalStrength - 113 // -> dBm
                Log.d(this.javaClass.name, "Pure GSM Signal Strength: $sigStrength.gsmSignalStrength, " +
                        "Post-Treatment GSM Signal Strength: $m_signalStrength")

                try {
                    m_signalLevel = sigStrength.javaClass.getMethod("getGsmLevel").invoke(sigStrength) as Int
                    Log.d(this.javaClass.name, "Signal Strength Level: $m_signalLevel")
                } catch (ex: Exception) {
                    Log.v("Error", "Couldn't retrieve signal level - " + ex.message)
                }
            }
            callback()
        }
    }

    fun checkPermissions(context: Context): Boolean {
        return pmanager.checkPermissions(context, Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION)
    }

    @JvmOverloads
    fun listen(context: Context, callback: () -> Unit = {}) {
        phoneStateListener.callback = callback

        if (checkPermissions(context)) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
        }
    }

    fun stop(context: Context) {
        if (checkPermissions(context)) {
            telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        }
    }

    fun once(context: Context, callback: () -> Unit) {
        listen(context) {
            if (isConsistent()) { // Listen until we get consistent data
                callback()
                stop(context)
            }
        }
    }

    fun isConsistent() : Boolean {
        return -150 <= signalStrength && signalStrength < -40 && "Unknown" != type;
    }

    private fun getLTESignalStrength(sigStrength: SignalStrength): Int {
        try {
            val methods = android.telephony.SignalStrength::class.java.methods

            for (mthd in methods) {
                if (mthd.name == "getLteSignalStrength") {
                    val LTESignalStrength = mthd.invoke(sigStrength) as Int
                    Log.i(this.javaClass.name, "getLteSignalStrength= " + (LTESignalStrength - 140))
                    return LTESignalStrength
                }
            }
        } catch (e: Exception) {
            Log.e(this.javaClass.name, "Exception: " + e.toString())
        }

        return 0 // Return appropriate signal strength error value in case of failure
    }

    val type: String
        get() = networkTypeSerializer.serialize(telephonyManager.networkType)

    val operator: String
        get() = telephonyManager.networkOperatorName

    val signalStrength: Int
        get() = m_signalStrength

    val signalLevel: Int
        get() = m_signalLevel
}
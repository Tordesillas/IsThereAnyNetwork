package eu.miaounyan.isthereanynetwork.service.telephony

import android.telephony.TelephonyManager

class NetworkTypeSerializer {
    fun serialize(networkType: Int): String {
        when (networkType) {
            TelephonyManager.NETWORK_TYPE_GPRS -> return "GPRS"
            TelephonyManager.NETWORK_TYPE_EDGE -> return "EDGE"
            TelephonyManager.NETWORK_TYPE_UMTS -> return "UMTS"
            TelephonyManager.NETWORK_TYPE_CDMA -> return "CDMA"
            TelephonyManager.NETWORK_TYPE_EVDO_0 -> return "EVDO rev. 0"
            TelephonyManager.NETWORK_TYPE_EVDO_A -> return "EVDO rev. A"
            TelephonyManager.NETWORK_TYPE_1xRTT -> return "1xRTT"
            TelephonyManager.NETWORK_TYPE_HSDPA -> return "HSDPA"
            TelephonyManager.NETWORK_TYPE_HSUPA -> return "HSUPA"
            TelephonyManager.NETWORK_TYPE_HSPA -> return "HSPA"
            TelephonyManager.NETWORK_TYPE_IDEN -> return "iDen"
            TelephonyManager.NETWORK_TYPE_EVDO_B -> return "EVDO rev. B"
            TelephonyManager.NETWORK_TYPE_LTE -> return "LTE"
            TelephonyManager.NETWORK_TYPE_EHRPD -> return "eHRPD"
            TelephonyManager.NETWORK_TYPE_HSPAP -> return "HSPA+"
            TelephonyManager.NETWORK_TYPE_GSM -> return "GSM"
            TelephonyManager.NETWORK_TYPE_TD_SCDMA -> return "TD_SCDMA"
            TelephonyManager.NETWORK_TYPE_IWLAN -> return "IWLAN"
            TelephonyManager.NETWORK_TYPE_UNKNOWN -> return "Unknown"
        }
        return "Unknown"
    }
}
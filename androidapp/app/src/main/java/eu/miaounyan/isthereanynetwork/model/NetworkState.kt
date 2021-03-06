package eu.miaounyan.isthereanynetwork.model

import android.os.Parcelable
import com.squareup.moshi.Json
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NetworkState(
        @field:Json(name = "latitude") val latitude: Double,
        @field:Json(name = "longitude") val longitude: Double,
        @field:Json(name = "signalStrength") val signalStrength: Int,
        @field:Json(name = "operatorName") val operatorName: String,
        @field:Json(name = "date") val date: String, //FIXME: check if we can use a date type directly
        @field:Json(name = "networkProtocol") val networkProtocol: String
) : Parcelable
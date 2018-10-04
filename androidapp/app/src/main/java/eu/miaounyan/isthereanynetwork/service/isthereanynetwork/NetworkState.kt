package eu.miaounyan.isthereanynetwork.service.isthereanynetwork

import com.squareup.moshi.Json

data class NetworkState(
        @field:Json(name = "latitude") val latitude: Double,
        @field:Json(name = "longitude") val longitude: Double,
        @field:Json(name = "signalStrength") val signalStrength: Double,
        @field:Json(name = "operatorName") val operatorName: String,
        @field:Json(name = "date") val date: String, //FIXME: check if we can use a date type directly
        @field:Json(name = "networkProtocol") val networkProtocol: String
)
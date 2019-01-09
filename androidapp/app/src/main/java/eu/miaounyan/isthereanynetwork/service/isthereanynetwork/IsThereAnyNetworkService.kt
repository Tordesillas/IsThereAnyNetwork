package eu.miaounyan.isthereanynetwork.service.isthereanynetwork

import io.reactivex.Observable
import retrofit2.http.*

interface IsThereAnyNetworkService {
    @GET("networkstate")
    fun getNetworkStates(@QueryMap options : IsThereAnyNetworkParams): Observable<List<NetworkState>>

    @POST("networkstate")
    fun sendNetworkState(@Body networkState: NetworkState): Observable<NetworkState>

    @GET("networkstate/average")
    fun getOperatorRanking(@QueryMap options : IsThereAnyNetworkParams): Observable<Map<String, Double>>

    @GET("networkmap")
    fun getNetworkMap(@QueryMap options : IsThereAnyNetworkParams): Observable<List<Int>>
}
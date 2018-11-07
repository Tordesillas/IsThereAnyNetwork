package eu.miaounyan.isthereanynetwork.service.isthereanynetwork

import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface IsThereAnyNetworkService {
    @GET("networkstate")
    fun getNetworkStates(): Observable<List<NetworkState>>

    @POST("networkstate")
    fun sendNetworkState(@Body networkState: NetworkState): Observable<NetworkState>

    @GET("networkstate/average")
    fun getOperatorRanking(): Observable<Map<String, Double>>
}
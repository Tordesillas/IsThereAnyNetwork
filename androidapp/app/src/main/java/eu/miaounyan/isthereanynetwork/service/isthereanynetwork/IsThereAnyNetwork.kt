package eu.miaounyan.isthereanynetwork.service.isthereanynetwork

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

class IsThereAnyNetwork {
    private val url = "https://isthereanynetwork.miaounyan.eu/";

    fun connect(): IsThereAnyNetworkService {
        val retrofit = Retrofit.Builder()
                .baseUrl(url)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
        return retrofit.create(IsThereAnyNetworkService::class.java)
    }

    fun createParams() : IsThereAnyNetworkParams {
        return IsThereAnyNetworkParams(mutableMapOf());
    }

    fun defaultParams() : IsThereAnyNetworkParams {
        return createParams()
                .withSignalStrengthLowerThan(-45)
                .withSignalStrengthGreaterThan(-145)
    }
}
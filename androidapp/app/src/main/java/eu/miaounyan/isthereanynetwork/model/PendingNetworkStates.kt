package eu.miaounyan.isthereanynetwork.model

import android.content.Context
import android.content.SharedPreferences
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

class PendingNetworkStates {
    private val moshi = Moshi.Builder().build()
    private val pendingNetworkStatesAdapter : JsonAdapter<List<NetworkState>> = moshi.adapter(Types.newParameterizedType(List::class.java, NetworkState::class.java))

    private fun getSharedPreferences(context: Context?) : SharedPreferences? {
        return context?.getSharedPreferences("IsThereAnyNetwork", 0)
    }

    fun fetch(context: Context?) : List<NetworkState> {
        return getSharedPreferences(context)?.getString("PendingNetworkState", "[]")?.let {
            pendingNetworkStatesAdapter.fromJson(it)
        } ?: emptyList()
    }

    fun add(context: Context?, networkState: NetworkState) {
        val pendingNetworkState = fetch(context).toMutableList()
        pendingNetworkState.add(networkState)
        getSharedPreferences(context)?.edit()?.run {
            putString("PendingNetworkState", pendingNetworkStatesAdapter.toJson(pendingNetworkState))
            apply()
        }
    }

    fun removeAll(context: Context?) {
        getSharedPreferences(context)?.edit()?.run {
            putString("PendingNetworkState", pendingNetworkStatesAdapter.toJson(emptyList()))
            apply()
        }
    }
}
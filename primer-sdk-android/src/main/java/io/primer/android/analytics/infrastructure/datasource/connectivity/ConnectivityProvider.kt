package io.primer.android.analytics.infrastructure.datasource.connectivity

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import androidx.annotation.RequiresApi
import io.primer.android.analytics.data.models.NetworkType

internal interface ConnectivityProvider {
    interface ConnectivityStateListener {
        fun onStateChange(state: NetworkState)
    }

    fun addListener(listener: ConnectivityStateListener)
    fun removeListener(listener: ConnectivityStateListener)

    fun getNetworkState(): NetworkState

    sealed class NetworkState {
        object NotConnectedState : NetworkState()

        sealed class ConnectedState(val networkType: NetworkType) : NetworkState() {

            @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
            data class Connected(val networkCapabilities: NetworkCapabilities) : ConnectedState(
                networkCapabilities.toNetworkType()
            )

            @Suppress("DEPRECATION")
            data class ConnectedLegacy(val networkInfo: NetworkInfo) : ConnectedState(
                networkInfo.toNetworkType()
            )
        }
    }

    companion object {
        fun createProvider(context: Context): ConnectivityProvider {
            val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                ConnectivityProviderImpl(cm)
            } else {
                ConnectivityProviderLegacyImpl(context, cm)
            }
        }
    }
}

internal fun NetworkCapabilities.toNetworkType() = when {
    hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
    hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.CELLULAR
    hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> NetworkType.ETHERNET
    else -> NetworkType.OTHER
}

@Suppress("DEPRECATION")
internal fun NetworkInfo.toNetworkType() = when (type) {
    ConnectivityManager.TYPE_WIFI -> NetworkType.WIFI
    ConnectivityManager.TYPE_MOBILE -> NetworkType.CELLULAR
    ConnectivityManager.TYPE_ETHERNET -> NetworkType.ETHERNET
    else -> NetworkType.OTHER
}

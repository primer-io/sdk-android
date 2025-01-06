package io.primer.android.analytics.infrastructure.datasource.connectivity

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
internal class ConnectivityProviderImpl(private val cm: ConnectivityManager) :
    ConnectivityProviderBaseImpl() {
    private val networkCallback = ConnectivityCallback()

    override fun subscribe() {
        cm.registerDefaultNetworkCallback(networkCallback)
    }

    override fun unsubscribe() {
        cm.unregisterNetworkCallback(networkCallback)
    }

    override fun getNetworkState(): ConnectivityProvider.NetworkState {
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        return if (capabilities != null) {
            ConnectivityProvider.NetworkState.ConnectedState.Connected(capabilities)
        } else {
            ConnectivityProvider.NetworkState.NotConnectedState
        }
    }

    private inner class ConnectivityCallback : ConnectivityManager.NetworkCallback() {
        override fun onCapabilitiesChanged(
            network: Network,
            capabilities: NetworkCapabilities,
        ) {
            dispatchChange(ConnectivityProvider.NetworkState.ConnectedState.Connected(capabilities))
        }

        override fun onLost(network: Network) {
            dispatchChange(ConnectivityProvider.NetworkState.NotConnectedState)
        }
    }
}

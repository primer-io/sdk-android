package io.primer.android.analytics.infrastructure.datasource.connectivity

import android.net.ConnectivityManager

@Suppress("DEPRECATION")
internal class ConnectivityProviderLegacyImpl(
    private val cm: ConnectivityManager,
) : ConnectivityProvider {

    override fun getNetworkState(): ConnectivityProvider.NetworkState {
        val activeNetworkInfo = cm.activeNetworkInfo
        return if (activeNetworkInfo != null) {
            ConnectivityProvider.NetworkState.ConnectedState.ConnectedLegacy(activeNetworkInfo)
        } else {
            ConnectivityProvider.NetworkState.NotConnectedState
        }
    }
}

package io.primer.android.analytics.infrastructure.datasource.connectivity

import android.net.ConnectivityManager
import android.os.Build
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.N)
internal class ConnectivityProviderImpl(private val cm: ConnectivityManager) :
    ConnectivityProvider {

    override fun getNetworkState(): ConnectivityProvider.NetworkState {
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
        return if (capabilities != null) {
            ConnectivityProvider.NetworkState.ConnectedState.Connected(capabilities)
        } else {
            ConnectivityProvider.NetworkState.NotConnectedState
        }
    }
}

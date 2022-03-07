package io.primer.android.analytics.infrastructure.datasource

import android.content.Context
import io.primer.android.analytics.data.models.NetworkType
import io.primer.android.analytics.data.models.NetworkTypeProperties
import io.primer.android.analytics.infrastructure.datasource.connectivity.ConnectivityProvider
import io.primer.android.data.base.datasource.BaseFlowDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart

internal class NetworkTypeDataSource(private val context: Context) :
    BaseFlowDataSource<NetworkTypeProperties, Unit> {

    private val sharedFlow = MutableStateFlow<NetworkTypeProperties?>(null)
    private val listener by lazy {
        object : ConnectivityProvider.ConnectivityStateListener {
            override fun onStateChange(state: ConnectivityProvider.NetworkState) {
                try {
                    when (state) {
                        is ConnectivityProvider.NetworkState.ConnectedState -> sharedFlow.tryEmit(
                            NetworkTypeProperties(state.networkType)
                        )
                        else -> sharedFlow.tryEmit(NetworkTypeProperties(NetworkType.NONE))
                    }
                } catch (ignored: Exception) {
                }
            }
        }
    }

    private val connectivityProvider by lazy { ConnectivityProvider.createProvider(context) }

    override fun execute(input: Unit): Flow<NetworkTypeProperties> = sharedFlow.asStateFlow()
        .onStart {
            addListener()
        }
        .onCompletion {
            removeListener()
        }
        .filterNotNull()

    private fun addListener() = connectivityProvider.addListener(listener)

    private fun removeListener() = connectivityProvider.removeListener(listener)
}

package io.primer.android.components.implementation.completion

import io.primer.android.payments.core.helpers.PollingStartHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow

internal class DefaultPollingStartHandler : PollingStartHandler {

    private val _startPolling = MutableSharedFlow<PollingStartHandler.PollingStartData>()

    override val startPolling: Flow<PollingStartHandler.PollingStartData> = _startPolling

    override suspend fun handle(pollingStartData: PollingStartHandler.PollingStartData) {
        _startPolling.emit(pollingStartData)
    }
}

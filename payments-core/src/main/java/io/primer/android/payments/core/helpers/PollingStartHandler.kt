package io.primer.android.payments.core.helpers

import kotlinx.coroutines.flow.Flow

/**
 * Exposes a flow that emits whenever polling should be handled.
 */
interface PollingStartHandler {
    /**
     * Flow that emits whenever polling should start.
     */
    val startPolling: Flow<PollingStartData>

    /**
     * Dispatches the event.
     */
    suspend fun handle(pollingStartData: PollingStartData)

    data class PollingStartData(
        val statusUrl: String,
        val paymentMethodType: String,
    )
}

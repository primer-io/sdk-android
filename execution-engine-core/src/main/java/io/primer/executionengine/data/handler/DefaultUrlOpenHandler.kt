package io.primer.executionengine.data.handler

import io.primer.executionengine.domain.executor.ComponentResultEvent
import io.primer.executionengine.domain.handler.UrlCloseReason
import io.primer.executionengine.domain.handler.UrlOpenHandler
import io.primer.executionengine.domain.handler.UrlOpenLaunchRequest
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

internal class DefaultUrlOpenHandler : UrlOpenHandler {

    private val _launchRequest = MutableSharedFlow<UrlOpenLaunchRequest>(extraBufferCapacity = 1)
    override val launchRequest: SharedFlow<UrlOpenLaunchRequest> = _launchRequest.asSharedFlow()

    private val _componentEvents = MutableSharedFlow<ComponentResultEvent>(extraBufferCapacity = 1)
    override val componentEvents: SharedFlow<ComponentResultEvent> = _componentEvents.asSharedFlow()

    override fun launch(url: String, redirectUrls: List<String>?) {
        _launchRequest.tryEmit(UrlOpenLaunchRequest(url, redirectUrls))
    }

    /**
     * A browser close never resolves success: the schema decides via `browserClose.closeReason`
     * (`auto` -> confirm payment, `user` -> poll payment), so both variants emit `cancelled`.
     */
    override fun onClosed(closeReason: UrlCloseReason) {
        _componentEvents.tryEmit(
            ComponentResultEvent(
                value = VALUE_CANCELLED,
                eventType = EVENT_TYPE,
                data = mapOf(CLOSE_REASON_KEY to closeReason.value),
            ),
        )
    }

    override fun onResultError(uri: String) {
        _componentEvents.tryEmit(
            ComponentResultEvent(
                value = VALUE_ERROR,
                eventType = EVENT_TYPE,
            ),
        )
    }

    private companion object {
        const val EVENT_TYPE = "custom"
        const val VALUE_CANCELLED = "cancelled"
        const val VALUE_ERROR = "error"
        const val CLOSE_REASON_KEY = "closeReason"
    }
}

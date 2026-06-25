package io.primer.executionengine.data.handler

import io.primer.executionengine.domain.executor.ComponentResultEvent
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

    override fun onResultOk() {
        emitEvent(VALUE_SUCCESS)
    }

    override fun onResultCancelled() {
        emitEvent(VALUE_CANCELLED)
    }

    override fun onResultError(uri: String) {
        emitEvent(VALUE_ERROR)
    }

    private fun emitEvent(value: String) {
        _componentEvents.tryEmit(
            ComponentResultEvent(
                value = value,
                eventType = EVENT_TYPE,
            ),
        )
    }

    private companion object {
        const val EVENT_TYPE = "custom"
        const val VALUE_SUCCESS = "completed"
        const val VALUE_CANCELLED = "cancelled"
        const val VALUE_ERROR = "error"
    }
}

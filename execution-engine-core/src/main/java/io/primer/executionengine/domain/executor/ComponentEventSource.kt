package io.primer.executionengine.domain.executor

import kotlinx.coroutines.flow.SharedFlow

data class ComponentResultEvent(
    val value: String,
    val eventType: String,
)

interface ComponentEventSource {
    val componentEvents: SharedFlow<ComponentResultEvent>
}

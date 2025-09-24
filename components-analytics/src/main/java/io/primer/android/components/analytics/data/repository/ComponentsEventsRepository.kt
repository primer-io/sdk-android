package io.primer.android.components.analytics.data.repository

import io.primer.android.components.analytics.data.model.EventType

interface ComponentsEventsRepository {
    fun send(event: EventType, timestamp: Long = System.currentTimeMillis())
}

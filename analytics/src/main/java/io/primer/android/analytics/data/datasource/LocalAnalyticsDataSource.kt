package io.primer.android.analytics.data.datasource

import io.primer.android.analytics.data.models.AnalyticsEvent
import java.util.concurrent.ConcurrentLinkedQueue

internal class LocalAnalyticsDataSource private constructor() {
    private val events = ConcurrentLinkedQueue<AnalyticsEvent>()

    fun addEvent(input: AnalyticsEvent) =
        synchronized(this) {
            events.add(input)
        }

    fun addEvents(input: List<AnalyticsEvent>) =
        synchronized(this) {
            events.addAll(input)
        }

    fun get(): List<AnalyticsEvent> = synchronized(this) { events.toList() }

    fun remove(events: List<AnalyticsEvent>) =
        synchronized(this) {
            this.events.removeAll(events.toSet())
        }

    companion object {
        val instance by lazy { LocalAnalyticsDataSource() }
    }
}

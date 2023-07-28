package io.primer.android.analytics.data.datasource

import io.primer.android.analytics.data.models.BaseAnalyticsEventRequest

internal class LocalAnalyticsDataSource private constructor() {

    private val events = mutableListOf<BaseAnalyticsEventRequest>()

    fun addEvent(input: BaseAnalyticsEventRequest) = synchronized(this) {
        events.add(input)
    }

    fun addEvents(input: List<BaseAnalyticsEventRequest>) = synchronized(this) {
        events.addAll(input)
    }

    fun get(): List<BaseAnalyticsEventRequest> = synchronized(this) { events.toList() }

    fun remove(events: List<BaseAnalyticsEventRequest>) = synchronized(this) {
        this.events.removeAll(events)
    }

    companion object {

        val instance by lazy { LocalAnalyticsDataSource() }
    }
}

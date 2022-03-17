package io.primer.android.analytics.data.datasource

import io.primer.android.analytics.data.models.BaseAnalyticsEventRequest

internal class LocalAnalyticsDataSource private constructor() {

    private val events = mutableListOf<BaseAnalyticsEventRequest>()

    fun addEvent(input: BaseAnalyticsEventRequest) = events.add(input)

    fun addEvents(input: List<BaseAnalyticsEventRequest>) = events.addAll(input)

    fun get(): List<BaseAnalyticsEventRequest> = events.toList()

    fun remove(events: List<BaseAnalyticsEventRequest>) = this.events.removeAll(events)

    companion object {

        val instance by lazy { LocalAnalyticsDataSource() }
    }
}

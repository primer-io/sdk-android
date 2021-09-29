package io.primer.android.events

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainCoroutineDispatcher

internal class EventDispatcher(
    private val coroutineDispatcher: MainCoroutineDispatcher = Dispatchers.Main
) {

    fun dispatchEvent(event: CheckoutEvent) {
        coroutineDispatcher.dispatch(coroutineDispatcher.immediate) {
            EventBus.broadcast(event)
        }
    }

    fun dispatchEvents(events: List<CheckoutEvent>) {
        coroutineDispatcher.dispatch(coroutineDispatcher.immediate) {
            events.forEach { event -> EventBus.broadcast(event) }
        }
    }
}

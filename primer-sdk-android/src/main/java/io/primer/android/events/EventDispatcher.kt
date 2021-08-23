package io.primer.android.events

internal class EventDispatcher {

    fun dispatchEvents(events: List<CheckoutEvent>) {
        events.forEach { event -> EventBus.broadcast(event) }
    }
}

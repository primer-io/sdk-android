package io.primer.android.events

internal class EventDispatcher {

    fun dispatchEvent(event: CheckoutEvent) {
        EventBus.broadcast(event)
    }

    fun dispatchEvents(events: List<CheckoutEvent>) {
        events.forEach { event -> EventBus.broadcast(event) }
    }
}

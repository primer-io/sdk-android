package io.primer.android.events

@Deprecated(message = "EventBus needs to be removed.") // FIXME drop event bus
internal object EventBus {

    val subscribers = ArrayList<EventListener>()

    interface EventListener {

        fun onEvent(e: CheckoutEvent)
    }

    interface SubscriptionHandle {

        fun unregister()
    }

    fun subscribe(l: EventListener): SubscriptionHandle {
        subscribers.add(l)

        return object : SubscriptionHandle {
            override fun unregister() {
//                subscribers.remove(l)
                /*
                The above code does not deregister the listener properly and
                results in possible duplication of listeners.
                Documentation assumes only one listener object at any moment
                so it's better that we clear all listeners whenever we unsubscribe
                in order to avoid the scenario of onTokenizeSuccess being called twice.
                */
                subscribers.clear()
            }
        }
    }

    fun subscribe(l: ((CheckoutEvent) -> Unit)): SubscriptionHandle {
        return subscribe(
            object : EventListener {
                override fun onEvent(e: CheckoutEvent) = l(e)
            }
        )
    }

    fun broadcast(e: CheckoutEvent) {
        subscribers.forEach { it.onEvent(e) }
    }
}

package io.primer.android.events

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
      override fun unregister() { subscribers.remove(l) }
    }
  }

  fun subscribe(l: ((CheckoutEvent) -> Unit)): SubscriptionHandle {
    return subscribe(object : EventListener {
      override fun onEvent(e: CheckoutEvent) = l(e)
    })
  }

  fun broadcast(e: CheckoutEvent) {
    subscribers.forEach { it.onEvent(e) }
  }
}
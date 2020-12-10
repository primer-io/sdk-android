package io.primer.android.events

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent

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
        subscribers.remove(l)
      }
    }
  }

  fun broadcast(e: CheckoutEvent) {
    subscribers.forEach { it.onEvent(e) }
  }
}
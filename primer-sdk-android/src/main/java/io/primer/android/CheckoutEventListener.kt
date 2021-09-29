package io.primer.android

import io.primer.android.events.CheckoutEvent

interface CheckoutEventListener {

    fun onCheckoutEvent(e: CheckoutEvent)
}

package io.primer.android.ui.deeplink.async

import android.os.Bundle
import io.primer.android.BaseCheckoutActivity
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventBus

internal class AsyncPaymentMethodDeeplinkActivity : BaseCheckoutActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.broadcast(CheckoutEvent.AsyncFlowRedirect)
        finish()
    }
}

package io.primer.android.ui.deeplink.async

import android.os.Bundle
import io.primer.android.BaseCheckoutActivity

internal class AsyncPaymentMethodDeeplinkActivity : BaseCheckoutActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }
}

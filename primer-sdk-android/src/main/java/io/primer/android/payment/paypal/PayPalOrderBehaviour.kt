package io.primer.android.payment.paypal

import android.net.Uri
import io.primer.android.payment.WebBrowserIntentBehaviour
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class PayPalOrderBehaviour(
    private val paypal: PayPalDescriptor,
) : WebBrowserIntentBehaviour() {

    override fun initialize() {
        tokenizationViewModel?.resetPaymentMethod(paypal)
    }

    override fun getUri(cancelUrl: String, returnUrl: String) {
        paypal.config.id?.let { id ->
            tokenizationViewModel?.createPayPalOrder(id, returnUrl, cancelUrl)
        }
    }

    override fun onSuccess(uri: Uri) {
        paypal.setTokenizableValue("paypalOrderId", uri.getQueryParameter("token") ?: "")
    }

    override fun onCancel(uri: Uri?) {
    }
}

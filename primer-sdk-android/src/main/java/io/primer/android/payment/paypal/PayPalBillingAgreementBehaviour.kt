package io.primer.android.payment.paypal

import android.net.Uri
import io.primer.android.logging.Logger
import io.primer.android.payment.WebBrowserIntentBehaviour
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class PayPalBillingAgreementBehaviour constructor(
    private val paypal: PayPalDescriptor,
) : WebBrowserIntentBehaviour() {

    private val log = Logger("paypal.billingagreement")

    override fun initialize() {
        // FIXME it should be passed instead like so: tokenize(paymentMethod)
        tokenizationViewModel?.resetPaymentMethod(paypal)
    }

    override fun getUri(cancelUrl: String, returnUrl: String) {
        paypal.config.id?.let { id ->
            tokenizationViewModel?.createPayPalBillingAgreement(id, returnUrl, cancelUrl)
        }
    }

    override fun onSuccess(uri: Uri) {
        uri.getQueryParameter("ba_token")?.let { token ->
            paypal.config.id?.let { id ->
                tokenizationViewModel?.confirmPayPalBillingAgreement(id, token)
            }
        }
    }

    override fun onCancel(uri: Uri?) {
        log.warn("User cancelled paypal billing agreement")
    }
}

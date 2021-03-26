package io.primer.android.payment.paypal

import android.net.Uri
import io.primer.android.logging.Logger
import io.primer.android.payment.WebBrowserIntentBehaviour
import io.primer.android.viewmodel.PrimerViewModel
import org.json.JSONObject
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class PayPalBillingAgreementBehaviour constructor(
    private val paypal: PayPal,
    private val viewModel: PrimerViewModel, // FIXME how can we avoid holding the viewmodel here?
) : WebBrowserIntentBehaviour() {

    private val log = Logger("paypal.billingagreement")

    override fun initialize() {
        // payment method to be tokenized is set here // FIXME it should be passed instead like so: tokenize(paymentMethod)
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

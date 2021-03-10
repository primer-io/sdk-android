package io.primer.android.payment.paypal

import android.net.Uri
import io.primer.android.logging.Logger
import io.primer.android.model.Observable
import io.primer.android.payment.WebBrowserIntentBehaviour
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class PayPalOrderBehaviour(
    private val paypal: PayPal,
) : WebBrowserIntentBehaviour() {

    private val log = Logger("paypal-orders")

    override fun initialize() {
        tokenizationViewModel?.reset(paypal)
    }

    override fun getUri(cancelUrl: String, returnUrl: String, callback: ((String) -> Unit)) {
        paypal.config.id?.let {
            tokenizationViewModel?.createPayPalOrder(
                id = it,
                returnUrl = returnUrl,
                cancelUrl = cancelUrl
            )?.observe { e ->
                when (e) {
                    is Observable.ObservableSuccessEvent -> {
                        callback(e.data.getString("approvalUrl"))
                    }
                }
            }
        }
    }

    override fun onSuccess(uri: Uri) {
        paypal.setTokenizableValue("paypalOrderId", uri.getQueryParameter("token") ?: "")
    }

    override fun onCancel(uri: Uri?) {
    }
}

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

    override fun initialize() {
        tokenizationViewModel?.reset(paypal)
    }

//    override fun getUri(cancelUrl: String, returnUrl: String, callback: ((String) -> Unit)) {
    override fun getUri(cancelUrl: String, returnUrl: String) {
        paypal.config.id?.let { id ->

            tokenizationViewModel?._createPayPalOrder(id, returnUrl, cancelUrl)
//            tokenizationViewModel?.createPayPalOrder(id, returnUrl, cancelUrl)?.observe { e ->
//                when (e) {
//                    is Observable.ObservableSuccessEvent -> {
//                        val url = e.data.getString("approvalUrl")
//                        callback(url)
//                    }
//                }
//            }
        }
    }

    override fun onSuccess(uri: Uri) {
        paypal.setTokenizableValue("paypalOrderId", uri.getQueryParameter("token") ?: "")
    }

    override fun onCancel(uri: Uri?) {
    }
}

package io.primer.android.payment.klarna

import android.net.Uri
import io.primer.android.payment.WebBrowserIntentBehaviour

internal class RecurringKlarnaBehaviour constructor(
    private val klarna: KlarnaDescriptor,
) : WebBrowserIntentBehaviour() {

    override fun initialize() {
        tokenizationViewModel?.resetPaymentMethod(klarna)
    }

    override fun getUri(cancelUrl: String, returnUrl: String) {
        klarna.config.id?.let { id ->
            tokenizationViewModel?.createKlarnaPaymentSession(id, returnUrl, klarna)
        }
    }

    override fun onSuccess(uri: Uri) = Unit

    override fun onCancel(uri: Uri?) = Unit
}

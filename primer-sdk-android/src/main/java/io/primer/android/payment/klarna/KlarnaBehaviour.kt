package io.primer.android.payment.klarna

import android.net.Uri
import android.util.Log
import io.primer.android.payment.WebBrowserIntentBehaviour
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class KlarnaBehaviour constructor(
    private val klarna: Klarna
) : WebBrowserIntentBehaviour() {

    override fun initialize() {
        tokenizationViewModel?.resetPaymentMethod(klarna)
    }

    override fun getUri(cancelUrl: String, returnUrl: String) {
        klarna.config.id?.let { id ->
            tokenizationViewModel?.createKlarnaBillingAgreement(id, returnUrl)
        }
    }

    override fun onSuccess(uri: Uri) {
        Log.d("RUI", "KlarnaBehaviour onSuccess: ")
        uri.getQueryParameter("ba_token")?.let { token ->
            klarna.config.id?.let { id ->
                tokenizationViewModel?.confirmPayPalBillingAgreement(id, token)
            }
        }
    }

    override fun onCancel(uri: Uri?) {
        // TODO what should we do here?
    }
}

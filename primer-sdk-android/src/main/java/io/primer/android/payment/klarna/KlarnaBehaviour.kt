package io.primer.android.payment.klarna

import android.net.Uri
import io.primer.android.logging.Logger
import io.primer.android.payment.WebBrowserIntentBehaviour
import io.primer.android.viewmodel.PrimerViewModel
import org.json.JSONObject
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
            tokenizationViewModel?.createPayPalBillingAgreement(id, returnUrl, cancelUrl)
        }
    }

    override fun onSuccess(uri: Uri) {
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

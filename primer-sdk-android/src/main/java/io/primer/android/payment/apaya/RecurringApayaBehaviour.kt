package io.primer.android.payment.apaya

import android.net.Uri
import io.primer.android.payment.WebBrowserIntentBehaviour
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal data class RecurringApayaBehaviour constructor(
    private val apaya: ApayaDescriptor,
) : WebBrowserIntentBehaviour() {

    override fun initialize() {
        tokenizationViewModel?.resetPaymentMethod(apaya)
    }

    override fun getUri(cancelUrl: String, returnUrl: String) {
        apaya.config.let { config ->
            tokenizationViewModel?.getApayaToken(
                config.options?.merchantAccountId.orEmpty(),
            )
        }
    }

    override fun onSuccess(uri: Uri) {
        // no-op
    }

    override fun onCancel(uri: Uri?) {
        tokenizationViewModel?.userCanceled(apaya.config.type)
    }
}

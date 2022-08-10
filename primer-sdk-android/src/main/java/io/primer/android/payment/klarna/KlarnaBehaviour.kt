package io.primer.android.payment.klarna

import android.net.Uri
import io.primer.android.payment.WebBrowserIntentBehaviour
import io.primer.android.payment.WebViewBehaviour
import io.primer.android.viewmodel.TokenizationViewModel
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal class KlarnaBehaviour constructor(
    private val klarna: KlarnaDescriptor,
    packageName: String,
) : WebViewBehaviour(packageName) {

    override fun initialize(viewModel: TokenizationViewModel) {
        viewModel.resetPaymentMethod(klarna)
    }

    override fun getUri(viewModel: TokenizationViewModel, returnUrl: String) {
        klarna.config.id?.let { id ->
            viewModel.createKlarnaPaymentSession(id, returnUrl, klarna)
        }
    }
}

@KoinApiExtension
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

    override fun onSuccess(uri: Uri) {
        val klarnaAuthToken = uri.getQueryParameter("token") ?: ""
        tokenizationViewModel?.handleRecurringKlarnaRequestResult(klarna, klarnaAuthToken)
    }

    override fun onCancel(uri: Uri?) {
        tokenizationViewModel?.userCanceled(klarna.config.type)
    }
}

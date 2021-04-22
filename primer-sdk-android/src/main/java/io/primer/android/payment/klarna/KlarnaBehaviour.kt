package io.primer.android.payment.klarna

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

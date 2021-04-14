package io.primer.android.payment

import io.primer.android.viewmodel.TokenizationViewModel
import org.koin.core.component.KoinApiExtension
import java.util.UUID

@KoinApiExtension
internal abstract class WebViewBehaviour(
    private val packageName: String,
) : SelectedPaymentMethodBehaviour() {

    fun execute(viewModel: TokenizationViewModel) {
        initialize(viewModel)

        val id = UUID.randomUUID().toString()
        val returnUrl = "$packageName.primer://$id/success"

        getUri(viewModel, returnUrl)
    }

    abstract fun initialize(viewModel: TokenizationViewModel)

    // FIXME @RUI rename to createKlarnaBillingAgreement()
    abstract fun getUri(viewModel: TokenizationViewModel, returnUrl: String)
}

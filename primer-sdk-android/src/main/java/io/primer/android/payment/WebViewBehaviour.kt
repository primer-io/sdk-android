package io.primer.android.payment

import android.net.Uri
import io.primer.android.viewmodel.TokenizationViewModel
import org.koin.core.component.KoinApiExtension
import java.util.*

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
    abstract fun getUri(viewModel: TokenizationViewModel, returnUrl: String)
}

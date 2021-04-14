package io.primer.android.payment

import android.net.Uri
import io.primer.android.WebviewInteropRegister
import io.primer.android.viewmodel.TokenizationViewModel
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
internal abstract class WebBrowserIntentBehaviour : SelectedPaymentMethodBehaviour() {

    // FIXME how can we avoid holding the viewmodel here?
    protected var tokenizationViewModel: TokenizationViewModel? = null

    fun execute(viewModel: TokenizationViewModel) {
        tokenizationViewModel = viewModel

        initialize()

        val interopRegister = WebviewInteropRegister.register(this)

        getUri(interopRegister.cancelUrl, interopRegister.successUrl)
    }

    abstract fun initialize()
    abstract fun getUri(cancelUrl: String, returnUrl: String)

    abstract fun onSuccess(uri: Uri)
    abstract fun onCancel(uri: Uri? = null)
}

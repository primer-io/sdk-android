package io.primer.android.payment

import android.net.Uri
import androidx.fragment.app.Fragment
import io.primer.android.R
import io.primer.android.WebviewInteropRegister
import io.primer.android.viewmodel.TokenizationViewModel
import org.koin.core.component.KoinApiExtension

internal abstract class SelectedPaymentMethodBehaviour

internal class NewFragmentBehaviour(
    private val factory: (() -> Fragment),
    private val returnToPreviousOnBack: Boolean = false,
) : SelectedPaymentMethodBehaviour() {

    fun execute(parent: Fragment) {
        val fragment = factory()
        val transaction = parent.childFragmentManager.beginTransaction()

        if (returnToPreviousOnBack) {
            transaction.addToBackStack(null)
        }

        transaction.replace(R.id.checkout_sheet_content, fragment)

        transaction.commit()
    }
}

@KoinApiExtension
internal abstract class WebBrowserIntentBehaviour : SelectedPaymentMethodBehaviour() {

    protected var tokenizationViewModel: TokenizationViewModel? = null

    fun execute(
        // context: Context,
        viewModel: TokenizationViewModel,
        // callback: ((String) -> Unit)
    ) {
        tokenizationViewModel = viewModel

        initialize()

        val interopRegister = WebviewInteropRegister.register(this)
//        val callback = { uri: String ->
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
//            context.startActivity(intent)
//        }

        // the callback code is now in
        getUri(
            interopRegister.cancelUrl,
            interopRegister.successUrl,
//            callback
        )
    }

    abstract fun initialize()
    abstract fun getUri(
        cancelUrl: String,
        returnUrl: String,
        // callback: ((String) -> Unit)
    )

    abstract fun onSuccess(uri: Uri)
    abstract fun onCancel(uri: Uri? = null)
}

internal class NoopBehaviour : SelectedPaymentMethodBehaviour()

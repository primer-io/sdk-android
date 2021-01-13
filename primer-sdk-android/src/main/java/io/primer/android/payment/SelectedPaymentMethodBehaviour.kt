package io.primer.android.payment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import io.primer.android.R
import io.primer.android.WebviewInteropActivity
import io.primer.android.WebviewInteropRegister
import io.primer.android.model.Observable
import io.primer.android.viewmodel.PrimerViewModel
import io.primer.android.viewmodel.TokenizationViewModel

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

    transaction.setCustomAnimations(R.anim.default_transition_in, R.anim.default_transition_out)
    transaction.replace(R.id.checkout_sheet_content, fragment)
    transaction.commit()
  }
}

internal abstract class WebBrowserIntentBehaviour : SelectedPaymentMethodBehaviour() {
  protected var tokenizationViewModel: TokenizationViewModel? = null

  fun execute(context: Context, viewModel: TokenizationViewModel) {
    tokenizationViewModel = viewModel

    initialize()

    val callback = WebviewInteropRegister.register(this)

    getUri(callback.cancelUrl, callback.successUrl) { uri ->
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
      context.startActivity(intent)
    }
  }

  abstract fun initialize()
  abstract fun getUri(cancelUrl: String, returnUrl: String, callback: ((String) -> Unit))
  abstract fun onSuccess(uri: Uri)
  abstract fun onCancel(uri: Uri? = null)
}

internal class NoopBehaviour : SelectedPaymentMethodBehaviour()
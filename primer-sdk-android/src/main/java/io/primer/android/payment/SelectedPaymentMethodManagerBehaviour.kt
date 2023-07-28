package io.primer.android.payment

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import io.primer.android.PrimerSessionIntent
import io.primer.android.components.manager.nativeUi.PrimerHeadlessUniversalCheckoutNativeUiManager

internal class SelectedPaymentMethodManagerBehaviour(
    private val paymentMethodType: String,
    private val sessionIntent: PrimerSessionIntent
) : SelectedPaymentMethodBehaviour() {

    fun execute(context: AppCompatActivity, viewModel: ViewModel) {
        PrimerHeadlessUniversalCheckoutNativeUiManager.newInstance(paymentMethodType).also {
            viewModel.addCloseable {
                it.cleanup()
            }
        }.also {
            it.showPaymentMethod(context, sessionIntent)
        }
    }
}

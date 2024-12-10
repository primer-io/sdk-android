@file:OptIn(ExperimentalCoroutinesApi::class)

package io.primer.android.payment

import io.primer.android.paymentMethods.PaymentMethodBehaviour
import io.primer.android.viewmodel.PrimerViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi

internal class NativeUiPaymentMethodManagerCancellationBehaviour : PaymentMethodBehaviour {

    operator fun invoke(viewModel: PrimerViewModel) {
        viewModel.clearSelectedPaymentMethodNativeUiManager()
    }
}

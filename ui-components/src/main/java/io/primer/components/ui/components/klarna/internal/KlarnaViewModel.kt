package io.primer.components.ui.components.klarna.internal

import io.primer.components.implementation.base.PaymentMethodViewModel
import io.primer.components.ui.components.klarna.KlarnaPaymentMethodScope
import io.primer.components.ui.components.klarna.KlarnaPaymentUiState

internal class KlarnaViewModel : PaymentMethodViewModel<KlarnaPaymentUiState>(), KlarnaPaymentMethodScope {
    init {
        _state.value = KlarnaPaymentUiState.Loading
    }

    override fun onPaymentOptionsChange(option: KlarnaPaymentMethodScope.KlarnaPaymentOptions) {
        // TODO
    }

    override fun submit() {
        // TODO
    }

    override fun finalizePayment() {
        // TODO
    }

    override fun cancel() {
        // TODO
    }
}

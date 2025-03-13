package io.primer.components.ui.components.card.internal

import io.primer.android.configuration.data.model.CardNetwork
import io.primer.components.implementation.base.PaymentMethodViewModel
import io.primer.components.ui.components.card.CardPaymentMethodScope
import io.primer.components.ui.components.card.CardPaymentUiState

internal class CardViewModel : PaymentMethodViewModel<CardPaymentUiState>(), CardPaymentMethodScope {
    init {
        _state.value = CardPaymentUiState.Loading
    }

    override fun onCardNumberChange(value: String) {
        // TODO
    }

    override fun onCardExpirationChange(value: String) {
        // TODO
    }

    override fun onCvvChange(value: String) {
        // TODO
    }

    override fun onCardholderNameChange(value: String) {
        // TODO
    }

    override fun onCardNetworkChange(value: CardNetwork.Type) {
        // TODO
    }

    override fun submit() {
        // TODO
    }

    override fun cancel() {
        // TODO
    }
}

package io.primer.components.ui.components.card

import io.primer.components.PrimerPaymentMethodScope

// TODO: add KDocs after stabilizing interface
sealed interface CardPaymentUiState : PrimerPaymentMethodScope.PrimerPaymentMethodUiState {
    data object Loading : CardPaymentUiState

    data class Loaded(
        val cardNumber: String,
        val cvv: String,
        val expiration: String,
        val cardholderName: String,
    ) : CardPaymentUiState

    // TODO: Flaviu to replace this with actual UI state implementation from a different branch
}

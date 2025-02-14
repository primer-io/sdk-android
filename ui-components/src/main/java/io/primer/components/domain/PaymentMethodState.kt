package io.primer.components.domain

import io.primer.components.domain.models.PaymentValidationState

sealed interface PaymentMethodState {
    val validationState: PaymentValidationState
    val isLoading: Boolean

    data class RedirectState(
        override val validationState: PaymentValidationState = PaymentValidationState(isValid = true),
        override val isLoading: Boolean = false,
    ) : PaymentMethodState

    data class FormState(
        override val validationState: PaymentValidationState = PaymentValidationState(),
        override val isLoading: Boolean = false,
    ) : PaymentMethodState
}

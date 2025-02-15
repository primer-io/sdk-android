package io.primer.components.domain

import io.primer.components.domain.models.PaymentValidationState

/**
 * A sealed interface representing the state of a payment method during the checkout process.
 */
sealed interface PaymentMethodState {

    /**
     * The validation state of the payment method.
     */
    val validationState: PaymentValidationState

    /**
     * Indicates whether the payment method is currently in a loading state.
     */
    val isLoading: Boolean

    /**
     * Represents the state for a payment method that requires a redirecting to either a web-view or to a 3rd party SDK
     * for handling.
     */
    data class RedirectState(
        override val validationState: PaymentValidationState = PaymentValidationState(isValid = true),
        override val isLoading: Boolean = false,
    ) : PaymentMethodState

    /**
     * Represents the state for a payment method that involves form-based interaction.
     */
    data class FormState(
        override val validationState: PaymentValidationState = PaymentValidationState(),
        override val isLoading: Boolean = false,
    ) : PaymentMethodState
}

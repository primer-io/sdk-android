package io.primer.components.checkout

import io.primer.components.models.paymentMethods.PaymentMethod
import kotlinx.coroutines.flow.StateFlow

/**
 * Scope provided when customizing checkout via [PrimerCheckout]'s `content` block.
 * Gives access to state and payment method selection.
 */
interface PrimerCheckoutScope {
    /**
     * Represents current checkout state
     */
    val state: StateFlow<State>

    /**
     * Sets the selected payment method for checkout.
     */
    fun selectPaymentMethod(method: PaymentMethod<*>)

    /**
     * Clears selected payment method.
     */
    fun clearSelectedPaymentMethod()

    /**
     * Represents checkout states: loading, ready, or method selected.
     */
    sealed interface State {
        /**
         * The checkout flow is initializing and the available payment methods have not yet been loaded.
         */
        data object Loading : State

        /**
         * The checkout flow has been initialized, and available payment methods are now ready.
         * Contains a list of [PaymentMethod] objects representing the methods available for selection.
         */
        data class Ready(val paymentMethods: List<PaymentMethod<*>>) : State

        /**
         * The user has selected a payment method from the list.
         */
        data class Selected(val paymentMethod: PaymentMethod<*>) : State
    }
}



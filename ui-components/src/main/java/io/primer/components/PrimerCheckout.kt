package io.primer.components

import io.primer.components.clean.model.PrimerPaymentMethod
import kotlinx.coroutines.flow.StateFlow

/**
 * Interface providing control and state observation for the Primer checkout experience.
 *
 * This interface is exposed within the `content` lambda of [Primer.ComposableCheckout],
 * allowing custom UI implementations to interact with the checkout flow while maintaining
 * access to payment state and methods.
 *
 * ## Example Usage
 * ```kotlin
 * Primer.ComposableCheckout(
 *     content = {
 *         // Access to PrimerCheckout scope
 *         val checkoutState by state.collectAsState()
 *
 *         when (checkoutState) {
 *             is State.Ready -> {
 *                 // Display payment methods
 *                 checkoutState.paymentMethods.forEach { method ->
 *                     PaymentMethodCard(
 *                         method = method,
 *                         onClick = { selectPaymentMethod(method) }
 *                     )
 *                 }
 *             }
 *             is State.Loading -> CircularProgressIndicator()
 *             is State.Error -> ErrorMessage(checkoutState.message)
 *         }
 *     }
 * )
 * ```
 */
interface PrimerCheckout {

    /**
     * Observable state of the checkout process.
     *
     * Emits [State] updates as the checkout progresses through loading,
     * displaying available payment methods, or encountering errors.
     *
     * Collect this flow to react to checkout state changes in your UI.
     */
    val state: StateFlow<State>

    /**
     * Selects a payment method for the current checkout session.
     *
     * Call this when the user taps on a payment method to begin the payment flow
     * with that method. This may trigger additional UI like forms or 3DS challenges
     * depending on the payment method requirements.
     *
     * @param method The [PrimerPaymentMethod] to use for payment
     */
    fun selectPaymentMethod(method: PrimerPaymentMethod)

    /**
     * Clears the currently selected payment method.
     *
     * Use this to allow users to change their payment method selection
     * or to reset the checkout state after an error.
     */
    fun clearSelectedPaymentMethod()

    /**
     * Performs cleanup of checkout resources.
     *
     * This is typically called automatically when the checkout composable
     * leaves the composition. Only call this manually if you need to
     * force cleanup while the checkout is still composed.
     */
    fun cleanup()

    /**
     * Represents the various states of the checkout process.
     */
    sealed interface State {

        /**
         * Initial state while payment methods are being loaded.
         *
         * Display a loading indicator during this state.
         */
        data object Loading : State

        /**
         * Payment methods have been successfully loaded and are ready for selection.
         *
         * @property paymentMethods List of available payment methods for the current session.
         *                          May be filtered based on merchant configuration and user eligibility.
         */
        data class Ready(val paymentMethods: List<PrimerPaymentMethod>) : State

        /**
         * An error occurred during the checkout process.
         *
         * @property message Human-readable error message to display to the user.
         *                   Consider providing retry options when displaying this state.
         */
        data class Error(val message: String) : State
    }
}

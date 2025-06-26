package io.primer.android.scope

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

/**
 * Defines the scope for Primer's checkout flow, providing access to state management,
 * UI customization, and nested component scopes.
 */
interface PrimerCheckoutScope {

    /**
     * StateFlow representing the current state of the checkout process.
     * Emits [State.Initializing], [State.Dismissed], or [State.Error] based on checkout progress.
     */
    val state: StateFlow<State>

    /**
     * Composable container that wraps the entire checkout UI.
     * Allows customization of the checkout's root container layout.
     *
     * @param content The checkout content to be wrapped by this container
     */
    var container: @Composable (content: @Composable () -> Unit) -> Unit

    /**
     * Composable function for displaying the splash screen during checkout initialization.
     * Shown while the SDK prepares payment methods and configuration.
     */
    var splashScreen: @Composable () -> Unit

    /**
     * Composable function for displaying loading states during payment processing.
     * Shown during network requests, payment validation, and processing steps.
     */
    var loadingScreen: @Composable () -> Unit

    /**
     * Composable function for displaying successful payment completion.
     * Shown when payment has been successfully processed and completed.
     */
    var successScreen: @Composable () -> Unit

    /**
     * Composable function for displaying error states with custom messaging.
     * Shown when errors occur during the checkout process.
     *
     * @param message Error message to display to the user
     */
    var errorScreen: @Composable (message: String) -> Unit

    /**
     * Scope for card form functionality, providing access to card input components
     * and validation within the checkout flow.
     */
    val cardForm: PrimerCardFormScope

    /**
     * Scope for payment method selection, providing access to payment method
     * listing and selection components within the checkout flow.
     */
    val paymentMethodSelection: PrimerPaymentMethodSelectionScope

    /**
     * Dismisses the checkout flow and cleans up associated resources.
     * Should be called when the user cancels or exits the checkout process.
     */
    fun onDismiss()

    /**
     * Represents the various states of the checkout process.
     */
    sealed interface State {

        /**
         * Initial state when checkout is being prepared and configured.
         * Payment methods are being loaded and validated.
         */
        data object Initializing : State

        /**
         * State indicating the checkout has been dismissed by the user
         * or programmatically closed.
         */
        data object Dismissed : State

        /**
         * Error state containing the exception that caused the checkout to fail.
         *
         * @param exception The throwable that caused the error state
         */
        data class Error(val exception: Throwable) : State
    }
}

package io.primer.android.scope

import io.primer.android.components.PrimerCheckoutComponents
import io.primer.android.core.di.DISdkComponent
import io.primer.android.core.di.extensions.resolve
import kotlinx.coroutines.flow.StateFlow

/**
 * Defines the scope for Primer's checkout flow, providing access to state management,
 * UI customization, and nested component scopes.
 */
interface PrimerCheckoutScope : DISdkComponent {

    val components: PrimerCheckoutComponents
        get() = resolve()

    /**
     * StateFlow representing the current state of the checkout process.
     * Emits [State.Initializing], [State.Ready], [State.Dismissed], or [State.Error] based on checkout progress.
     */
    val state: StateFlow<State>

    /**
     * Dismisses the checkout flow and cleans up associated resources.
     * Should be called when the user cancels or exits the checkout process.
     */
    fun onDismiss()

    suspend fun onRetry()

    suspend fun onOtherPaymentMethods()

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
         * Ready state when checkout has finished loading.
         * Payment methods are available.
         *
         * @param totalAmount The total amount in cents for the checkout
         * @param currencyCode The currency code (e.g., "USD", "EUR")
         */
        data class Ready(
            val totalAmount: Int,
            val currencyCode: String,
        ) : State

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

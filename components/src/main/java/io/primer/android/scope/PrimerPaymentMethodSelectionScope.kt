package io.primer.android.scope

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.android.ui.core.configuration.domain.model.BasicOrderInfo
import kotlinx.coroutines.flow.StateFlow

/**
 * Defines the scope for Primer's payment method selection functionality,
 * providing state management and UI customization for choosing payment methods.
 */
interface PrimerPaymentMethodSelectionScope {

    /**
     * StateFlow representing the current state of payment method selection,
     * including loading, ready with available methods, or error states.
     */
    val state: StateFlow<State>

    /**
     * Handles the selection of a payment method and proceeds to the next step.
     *
     * @param paymentMethod The identifier of the selected payment method
     */
    fun onPaymentMethodSelected(paymentMethod: String)

    /**
     * Cancels the whole checkout flow.
     */
    fun onCancel()

    /**
     * Represents the various states of payment method selection.
     */
    sealed interface State {
        /**
         * Loading state while payment methods are being fetched and prepared.
         */
        data object Loading : State

        /**
         * Ready state with available payment methods and order information.
         *
         * @param paymentMethods List of available payment methods for selection
         * @param orderInfo Basic order information including amount and currency
         */
        data class Ready(
            val paymentMethods: List<PrimerComposablePaymentMethod>,
            val orderInfo: BasicOrderInfo,
        ) : State

        /**
         * Error state when payment method loading or selection fails.
         *
         * @param exception The throwable that caused the error state
         */
        data class Error(val exception: Throwable) : State
    }

    /**
     * Composable function for the entire payment method selection screen layout.
     */
    var screen: @Composable () -> Unit

    /**
     * Composable function for the card button.
     *
     * @param modifier Modifier for styling the payment method card
     */
    var paymentMethodCard: @Composable (modifier: Modifier) -> Unit
}

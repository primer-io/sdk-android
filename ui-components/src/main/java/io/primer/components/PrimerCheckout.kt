package io.primer.components

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.domain.error.models.PrimerError
import io.primer.components.PaymentMethodScope.State
import io.primer.components.implementation.checkout.PrimerCheckoutViewModel
import io.primer.components.models.paymentMethods.PaymentMethod
import io.primer.components.ui.checkout.PrimerCheckoutSheet
import kotlinx.coroutines.flow.StateFlow

/**
 * The main entry point to Primer's component-based SDK for implementing checkout functionality.
 *
 * This composable provides three customization options:
 * 1. Complete UI customization via the [content] parameter
 * 2. Success state customization via the [successContent] parameter
 * 3. Failure state customization via the [failureContent] parameter
 *
 * @param context Application context required to perform UI related actions.
 * @param clientToken The client token required for API authorization.
 * @param successContent An optional composable that displays when checkout completes successfully.
 * @param failureContent An optional composable that displays when checkout fails, receiving the [PrimerError] that
 *                       explains the failure.
 * @param content An optional composable that completely overrides the default checkout flow. When provided, this
 *                composable receives a [PrimerCheckoutScope] for accessing checkout functionality.
 */
@Composable
fun PrimerCheckout(
    context: Context,
    clientToken: String,
    successContent: @Composable () -> Unit = {
        // TODO: add success content
    },
    failureContent: @Composable (cause: PrimerError) -> Unit = {
        // TODO: add failure content
    },
    content: @Composable PrimerCheckoutScope.() -> Unit = {
        PrimerCheckoutSheet()
    },
) {
    val checkoutScope = viewModel<PrimerCheckoutViewModel>()
    LaunchedEffect(clientToken) {
        checkoutScope.start(
            context = context,
            clientToken = clientToken,
            primerSettings = PrimerSettings()
        )
    }

    content(checkoutScope)
}

/**
 * Scope provided when customizing checkout via [PrimerCheckout]'s `content` block.
 * Gives access to state and payment method selection.
 */
@Immutable
interface PrimerCheckoutScope {
    /**
     * Represents current checkout state
     */
    val state: StateFlow<State>

    /**
     * Sets the selected payment method for checkout.
     */
    fun selectPaymentMethod(method: PaymentMethod)

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
        data class Ready(val paymentMethods: List<PaymentMethod>) : State

        /**
         * The user has selected a payment method from the list.
         */
        data class Selected(val paymentMethod: PaymentMethod) : State
    }
}

/**
 * Scope interface for interacting with a specific payment method's UI state and behavior.
 *
 * @param T The specific implementation of [State] that represents
 *          the UI state for this payment method.
 */
interface PaymentMethodScope {
    /**
     * [StateFlow] containing the current UI state for this payment method.
     *
     * This property provides observable access to the payment method's state, allowing UI components to react to state
     * changes. The state is of type [State],  which is the payment method-specific implementation of
     * [State] that contains all the relevant state information for this particular payment method.
     */
    val state: StateFlow<State?>

    /**
     * Submits the payment information for processing.
     * To be called when the user has completed entering payment details.
     */
    fun submit()

    /**
     * Cancels the current payment method flow.
     * To be called when the user wants to abort the payment process.
     */
    fun cancel()

    /**
     * Base interface for payment method UI state implementations implemented by all payment methods with their specific
     * state information.
     */
    interface State
}

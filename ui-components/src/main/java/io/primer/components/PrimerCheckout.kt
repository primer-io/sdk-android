package io.primer.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import io.primer.android.clientToken.core.token.data.model.ClientToken
import io.primer.android.domain.error.models.PrimerError
import io.primer.components.PrimerPaymentMethodScope.PrimerPaymentMethodUiState
import io.primer.components.implementation.checkout.PrimerCheckoutViewModel
import io.primer.components.models.PaymentMethod
import io.primer.components.ui.checkout.PrimerCheckoutSheet
import io.primer.components.ui.platform.LocalPrimerConfig
import kotlinx.coroutines.flow.StateFlow

/**
 * The main entry point to Primer's component-based SDK for implementing checkout functionality.
 *
 * This composable provides three customization options:
 * 1. Complete UI customization via the [content] parameter
 * 2. Success state customization via the [successContent] parameter
 * 3. Failure state customization via the [failureContent] parameter
 *
 * @param clientToken The client token required for API authorization.
 * @param successContent An optional composable that displays when checkout completes successfully. If null, the default
 *                       success UI is shown.
 * @param failureContent An optional composable that displays when checkout fails, receiving the [PrimerError] that
 *                       explains the failure. If null, the default error UI is shown.
 * @param content An optional composable that completely overrides the default checkout flow. When provided, this
 *                composable receives a [PrimerCheckoutScope] for accessing checkout functionality.
 */
@Composable
fun PrimerCheckout(
    clientToken: String,
    successContent: (@Composable () -> Unit)? = null, // TODO: use
    failureContent: (@Composable (cause: PrimerError) -> Unit)? = null, // TODO: use
    content: (@Composable PrimerCheckoutScope.() -> Unit)? = null,
) {
    val primerConfig = LocalPrimerConfig.current

    var isClientTokenProcessed by remember { mutableStateOf(false) }

    LaunchedEffect(clientToken) {
        runCatching {
            ClientToken.fromString(clientToken)
        }.onSuccess {
            primerConfig.clientTokenBase64 = clientToken
            isClientTokenProcessed = true
        }.onFailure {
            throw it
        }
    }

    if (isClientTokenProcessed) {
        val checkoutScope: PrimerCheckoutScope = viewModel<PrimerCheckoutViewModel>()
        content?.invoke(checkoutScope) ?: checkoutScope.PrimerCheckoutSheet()
    }
}

/**
 * Scope interface for customizing the Primer checkout experience.
 *
 * This interface provides access to payment method data and selection functionality when implementing a custom checkout
 * experience through [PrimerCheckout]'s `content` parameter composable.
 * It allows you to:
 * - Observe available payment methods through [paymentMethods]
 * - Observe and change the selected payment method
 */
@Immutable
interface PrimerCheckoutScope {
    /**
     * A [StateFlow] containing the list of available payment methods based on prior merchant configuration.
     *
     * Each [PaymentMethod] in this list contains data to allow for payment method identification along with UI
     * components for displaying the default experience or a fully custom one.
     */
    val paymentMethods: StateFlow<List<PaymentMethod<*>>>

    /**
     * A [StateFlow] representing the currently selected payment method.
     * Emits `null` if no payment method is selected.
     */
    val selectedPaymentMethod: StateFlow<PaymentMethod<*>?>

    /**
     * Updates the selected payment method for the active checkout flow.
     *
     * Use this function to set the payment method before initiating the checkout process.
     *
     * @param method The payment method to select, or `null` to clear the current selection
     */
    fun selectPaymentMethod(method: PaymentMethod<*>?)
}

/**
 * Scope interface for interacting with a specific payment method's UI state and behavior.
 *
 * @param T The specific implementation of [PrimerPaymentMethodUiState] that represents
 *          the UI state for this payment method.
 */
interface PrimerPaymentMethodScope<T : PrimerPaymentMethodScope.PrimerPaymentMethodUiState> {
    /**
     * [StateFlow] containing the current UI state for this payment method.
     *
     * This property provides observable access to the payment method's state, allowing UI components to react to state
     * changes. The state is of type [T],  which is the payment method-specific implementation of
     * [PrimerPaymentMethodUiState] that contains all the relevant state information for this particular payment method.
     */
    val state: StateFlow<T?>

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
    interface PrimerPaymentMethodUiState
}

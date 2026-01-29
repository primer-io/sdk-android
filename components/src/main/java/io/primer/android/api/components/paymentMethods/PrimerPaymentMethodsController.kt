package io.primer.android.api.components.paymentMethods

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import io.primer.android.api.state.PrimerCheckoutController
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.android.internal.presentation.checkout.CheckoutViewModel
import kotlinx.coroutines.flow.StateFlow

/**
 * State and actions for available payment methods.
 *
 * Provides access to configured payment methods and handles selection.
 * Use with [PrimerPaymentMethods] for default UI, or build custom layouts.
 *
 * Create via [rememberPaymentMethodsController].
 *
 * ## Example: Custom grid layout
 * ```kotlin
 * val state = rememberPaymentMethodState(checkout)
 * val methods by state.methods.collectAsStateWithLifecycle()
 *
 * LazyVerticalGrid(columns = GridCells.Fixed(2)) {
 *     items(methods) { method ->
 *         PaymentMethodCard(
 *             name = method.paymentMethodName ?: method.paymentMethodType,
 *             onClick = { state.select(method) }
 *         )
 *     }
 * }
 * ```
 */
@Stable
interface PrimerPaymentMethodsController {

    /**
     * Available payment methods configured for this checkout session.
     *
     * Observe via `collectAsState()` to update UI when methods are loaded.
     */
    val paymentMethods: StateFlow<List<PrimerComposablePaymentMethod>>

    /**
     * Select a payment method to start the payment flow.
     *
     * The SDK handles the appropriate flow based on payment method type:
     * - **Card** - Opens the card form
     * - **Klarna, PayPal, etc.** - Starts the native payment flow
     *
     * @param method The payment method to select
     */
    fun select(method: PrimerComposablePaymentMethod)
}

/**
 * Creates and remembers payment method state for the current checkout session.
 *
 * Use this to access available payment methods and handle selection.
 * For default UI, pass to [PrimerPaymentMethods]. For custom UI, observe
 * [PrimerPaymentMethodsController.paymentMethods] and call [PrimerPaymentMethodsController.select].
 *
 * ## Example: Horizontal payment method chips
 * ```kotlin
 * val state = rememberPaymentMethodState(checkout)
 * val methods by state.methods.collectAsStateWithLifecycle()
 *
 * LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
 *     items(methods) { method ->
 *         FilterChip(
 *             selected = false,
 *             onClick = { state.select(method) },
 *             label = { Text(method.paymentMethodName ?: method.paymentMethodType) }
 *         )
 *     }
 * }
 * ```
 *
 * @param checkout Checkout state from [io.primer.android.api.checkout.rememberPrimerCheckoutController]
 * @return [PrimerPaymentMethodsController] for observing methods and handling selection
 */
@Composable
fun rememberPaymentMethodsController(checkout: PrimerCheckoutController): PrimerPaymentMethodsController {
    val viewModel = checkout as CheckoutViewModel
    return viewModel
}

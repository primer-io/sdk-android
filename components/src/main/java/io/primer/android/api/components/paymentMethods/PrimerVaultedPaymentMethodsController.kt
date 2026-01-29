package io.primer.android.api.components.paymentMethods

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import io.primer.android.api.state.PrimerCheckoutController
import io.primer.android.domain.tokenization.models.PrimerVaultedPaymentMethod
import io.primer.android.internal.presentation.checkout.CheckoutViewModel
import kotlinx.coroutines.flow.StateFlow

/**
 * State and actions for saved (vaulted) payment methods.
 *
 * Provides access to customer's saved payment methods with actions for
 * payment and management. Use with [PrimerVaultedPaymentMethods] for default UI,
 * or build custom layouts.
 *
 * Create via [rememberVaultedPaymentMethodsController].
 *
 * ## Example: Custom saved cards list
 * ```kotlin
 * val state = rememberVaultedPaymentMethodState(checkout)
 * val methods by state.methods.collectAsStateWithLifecycle()
 *
 * Column {
 *     methods.forEach { method ->
 *         SavedCardRow(
 *             last4 = method.paymentInstrumentData.last4Digits,
 *             network = method.paymentInstrumentData.network,
 *             onPay = { state.select(method) },
 *             onDelete = { state.delete(method) }
 *         )
 *     }
 * }
 * ```
 */
@Stable
interface PrimerVaultedPaymentMethodsController {

    /**
     * Customer's saved payment methods.
     *
     * Observe via `collectAsState()` to update UI when methods change.
     * Empty list if customer has no saved methods.
     */
    val methods: StateFlow<List<PrimerVaultedPaymentMethod>>

    /**
     * Pay with a saved payment method.
     *
     * If the card requires CVV re-entry, a CVV input screen will be shown automatically.
     *
     * @param method The saved payment method to use
     */
    fun select(method: PrimerVaultedPaymentMethod)

    /**
     * Delete a saved payment method.
     *
     * Shows a confirmation dialog before deletion. The method is removed
     * from [methods] after successful deletion.
     *
     * @param method The saved payment method to delete
     */
    fun delete(method: PrimerVaultedPaymentMethod)

    /**
     * Show all saved payment methods in a management screen.
     *
     * Opens a screen where customers can view all saved methods,
     * select one for payment, or delete methods.
     */
    fun showAll()
}

/**
 * Creates and remembers saved payment method state for the current checkout session.
 *
 * Use this to access saved payment methods and handle selection/deletion.
 * For default UI, pass to [PrimerVaultedPaymentMethods]. For custom UI, observe
 * [PrimerVaultedPaymentMethodsController.methods] and use the action methods.
 *
 * ## Example: Swipe-to-delete saved cards
 * ```kotlin
 * val state = rememberVaultedPaymentMethodState(checkout)
 * val methods by state.methods.collectAsStateWithLifecycle()
 *
 * LazyColumn {
 *     items(methods, key = { it.id }) { method ->
 *         SwipeToDeleteCard(
 *             onDelete = { state.delete(method) }
 *         ) {
 *             SavedCardRow(
 *                 last4 = method.paymentInstrumentData.last4Digits,
 *                 onClick = { state.select(method) }
 *             )
 *         }
 *     }
 * }
 * ```
 *
 * @param checkout Checkout state from [io.primer.android.api.rememberPrimerCheckoutState]
 * @return [PrimerVaultedPaymentMethodsController] for observing methods and handling actions
 */
@Composable
fun rememberVaultedPaymentMethodsController(checkout: PrimerCheckoutController): PrimerVaultedPaymentMethodsController {
    val viewModel = checkout as CheckoutViewModel
    return remember(viewModel) {
        object : PrimerVaultedPaymentMethodsController {
            override val methods: StateFlow<List<PrimerVaultedPaymentMethod>> =
                viewModel.vaultedPaymentMethods

            override fun select(method: PrimerVaultedPaymentMethod) {
                viewModel.selectVaultedPaymentMethod(method)
            }

            override fun delete(method: PrimerVaultedPaymentMethod) {
                viewModel.deleteVaultedPaymentMethod(method)
            }

            override fun showAll() {
                viewModel.navigator.navigateToVaultManage()
            }
        }
    }
}

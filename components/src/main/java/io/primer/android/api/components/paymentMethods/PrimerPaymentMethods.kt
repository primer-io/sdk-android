package io.primer.android.api.components.paymentMethods

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.primer.android.LocalPrimerTheme
import io.primer.android.internal.domain.models.PrimerComposablePaymentMethod
import io.primer.android.internal.domain.utils.getValue
import io.primer.android.internal.presentation.checkout.CheckoutViewModel
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.DefaultPaymentMethodsSectionHeader
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.list.DefaultPaymentMethodItem
import io.primer.android.internal.presentation.screens.paymentMethodSelection.components.surcharge.SurchargeGroupCard

/**
 * List of available payment methods for selection.
 *
 * Displays payment methods configured for this checkout session. When a method
 * is selected, the SDK handles the appropriate flow (card form, Klarna, etc.).
 *
 * Supports surcharge display when configured - methods are automatically
 * grouped by surcharge amount.
 *
 * ## Basic usage
 * ```kotlin
 * val controller = rememberPaymentMethodsController(checkout)
 * PrimerPaymentMethods(controller)
 * ```
 *
 * ## Custom payment method items
 * ```kotlin
 * PrimerPaymentMethods(
 *     controller = controller,
 *     method = { paymentMethod, onClick ->
 *         Card(
 *             modifier = Modifier
 *                 .fillMaxWidth()
 *                 .clickable { onClick() }
 *         ) {
 *             Row(verticalAlignment = Alignment.CenterVertically) {
 *                 AsyncImage(model = paymentMethod.iconUrl, contentDescription = null)
 *                 Text(paymentMethod.paymentMethodName ?: paymentMethod.paymentMethodType)
 *             }
 *         }
 *     }
 * )
 * ```
 *
 * @param controller Payment methods controller from [rememberPaymentMethodsController]
 * @param modifier Modifier for the list container
 * @param header Header content above the payment method list
 * @param method Custom rendering for each payment method
 */
@Composable
fun PrimerPaymentMethods(
    controller: PrimerPaymentMethodsController,
    modifier: Modifier = Modifier,
    header: @Composable () -> Unit = { PaymentMethodsDefaults.SectionHeader() },
    method: @Composable (paymentMethod: PrimerComposablePaymentMethod, onClick: () -> Unit) -> Unit = { m, onClick ->
        PaymentMethodsDefaults.Method(m, onClick)
    },
) {
    val paymentMethods by controller.paymentMethods.collectAsStateWithLifecycle()
    val theme = LocalPrimerTheme.current
    val spacingTokens = theme.spacingTokens
    val formatAmount: (Int) -> String = { (controller as CheckoutViewModel).formatAmount(it) }

    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(spacingTokens.small),
    ) {
        when {
            paymentMethods.isEmpty() -> {
                PaymentMethodsDefaults.EmptyState()
            }

            else -> {
                val grouped = paymentMethods.groupBy { it.surcharge?.getValue() ?: 0 }
                val hasSurcharge = grouped.size > 1 || !grouped.containsKey(0)

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(spacingTokens.small),
                ) {
                    header()

                    if (hasSurcharge) {
                        // Show methods grouped by surcharge amount
                        grouped.toList().forEach { (_, methods) ->
                            if (methods.isNotEmpty()) {
                                SurchargeGroupCard(
                                    value = methods.first().surcharge?.getValue() ?: 0,
                                    paymentMethods = methods,
                                    onPaymentMethodSelected = { paymentMethodType ->
                                        val method = methods.find { it.paymentMethodType == paymentMethodType }
                                        method?.let { controller.select(it) }
                                    },
                                    formatAmount = formatAmount,
                                )
                            }
                        }
                    } else {
                        // Show flat list
                        (grouped[0] ?: emptyList()).forEach { paymentMethod ->
                            method(paymentMethod) { controller.select(paymentMethod) }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Default component implementations for [PrimerPaymentMethods].
 */
object PaymentMethodsDefaults {

    /**
     * Default section header with "Pay with" text.
     */
    @Composable
    fun SectionHeader() {
        DefaultPaymentMethodsSectionHeader()
    }

    /**
     * Payment method item with click handler.
     *
     * ## Example with controller:
     * ```kotlin
     * val controller = rememberPaymentMethodsController(checkout)
     * methods.forEach { method ->
     *     PaymentMethodListDefaults.Method(
     *         method = method,
     *         onClick = { controller.select(method) }
     *     )
     * }
     * ```
     *
     * ## Example with custom behavior:
     * ```kotlin
     * PaymentMethodListDefaults.Method(
     *     method = method,
     *     onClick = {
     *         analytics.log("selected", method.paymentMethodType)
     *         // then trigger payment...
     *     }
     * )
     * ```
     */
    @Composable
    fun Method(
        method: PrimerComposablePaymentMethod,
        onClick: () -> Unit,
    ) {
        DefaultPaymentMethodItem(
            primerMethod = method,
            onPaymentMethodSelected = { onClick() },
        )
    }

    /**
     * Default empty state message.
     */
    // TODO use stringResource
    @Composable
    fun EmptyState(modifier: Modifier = Modifier) {
        Text(
            text = "No payment methods available",
            modifier = modifier,
        )
    }
}

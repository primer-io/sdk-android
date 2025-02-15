package io.primer.components.domain

import androidx.compose.runtime.Composable
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import kotlinx.coroutines.flow.StateFlow

/**
 * A [PaymentFlowScope] provides access to payment-related data and actions within the checkout process, allowing
 * complete control over the selection of payment methods, thus enabling the creation of a fully customized checkout
 * experience.
 */
interface PaymentFlowScope {

    /**
     * A [StateFlow] containing the list of available payment methods.
     */
    val paymentMethods: StateFlow<List<PrimerHeadlessUniversalCheckoutPaymentMethod>>

    /**
     * A [StateFlow] representing the currently selected payment method, or null if none is selected.
     */
    val selectedMethod: StateFlow<PrimerHeadlessUniversalCheckoutPaymentMethod?>

    /**
     * Selects a payment method for the checkout session.
     *
     * @param method The payment method to be selected, or null to clear the selection.
     */
    fun selectPaymentMethod(method: PrimerHeadlessUniversalCheckoutPaymentMethod?)

    /**
     * Provides a composable UI for rendering content specific to a given payment method.
     *
     * @param method The payment method for which content should be displayed.
     * @param content A composable lambda that receives a [PaymentMethodContentScope], enabling the definition of a
     * custom UI for the selected payment method.
     */
    @Composable
    fun PaymentMethodContent(
        method: PrimerHeadlessUniversalCheckoutPaymentMethod,
        content: @Composable PaymentMethodContentScope.() -> Unit,
    )
}

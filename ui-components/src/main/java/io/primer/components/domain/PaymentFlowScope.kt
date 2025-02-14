package io.primer.components.domain

import androidx.compose.runtime.Composable
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import kotlinx.coroutines.flow.StateFlow

interface PaymentFlowScope {
    val paymentMethods: StateFlow<List<PrimerHeadlessUniversalCheckoutPaymentMethod>>
    val selectedMethod: StateFlow<PrimerHeadlessUniversalCheckoutPaymentMethod?>

    fun selectPaymentMethod(method: PrimerHeadlessUniversalCheckoutPaymentMethod?)

    @Composable
    fun PaymentMethodContent(
        method: PrimerHeadlessUniversalCheckoutPaymentMethod,
        content: @Composable PaymentMethodContentScope.() -> Unit,
    )
}

package io.primer.components.domain

import androidx.compose.runtime.Composable
import io.primer.android.components.domain.core.models.PrimerHeadlessUniversalCheckoutPaymentMethod
import io.primer.components.domain.models.PaymentResult
import kotlinx.coroutines.flow.StateFlow

interface PaymentMethodContentScope {
    val method: PrimerHeadlessUniversalCheckoutPaymentMethod
    val state: StateFlow<PaymentMethodState>

    fun submit(): Result<PaymentResult>

    @Composable
    fun DefaultContent()
}

package io.primer.composable.internal.domain.models

import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

internal data class PaymentMethod(
    val type: PaymentMethodType,
    val name: String,
    val description: String? = null,
    val isEnabled: Boolean = true,
    val configuration: Map<String, Any> = emptyMap()
)

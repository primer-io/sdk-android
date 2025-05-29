package io.primer.components.clean.internal.domain.models

import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

/**
 * Core business entity representing a payment method.
 * This is independent of UI concerns and framework dependencies.
 */
internal data class PaymentMethod(
    val id: String,
    val type: PaymentMethodType,
    val name: String,
    val description: String? = null,
    val isEnabled: Boolean = true,
    val configuration: Map<String, Any> = emptyMap()
) {
    fun isCard(): Boolean = type == PaymentMethodType.PAYMENT_CARD
    fun isWallet(): Boolean = listOf(
        PaymentMethodType.GOOGLE_PAY
    ).contains(type)
}

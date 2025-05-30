package io.primer.composable.internal.domain.models

import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import java.math.BigDecimal

internal data class Payment(
    val id: String,
    val amount: BigDecimal,
    val currency: String,
    val paymentMethodType: PaymentMethodType,
    val status: PaymentStatus,
    val createdAt: Long = System.currentTimeMillis()
) {
    enum class PaymentStatus {
        PENDING,
        PROCESSING,
        AUTHORIZED,
        CAPTURED,
        FAILED,
        CANCELLED
    }
}

package io.primer.android.domain.payments.create.model

import io.primer.android.data.payments.create.models.PaymentStatus

internal data class PaymentResult(
    val payment: Payment,
    val paymentStatus: PaymentStatus,
    val clientToken: String?
)

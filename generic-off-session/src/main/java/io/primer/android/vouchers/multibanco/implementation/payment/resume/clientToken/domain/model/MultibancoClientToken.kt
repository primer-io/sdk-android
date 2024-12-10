package io.primer.android.vouchers.multibanco.implementation.payment.resume.clientToken.domain.model

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeClientToken

internal data class MultibancoClientToken(
    override val clientTokenIntent: String,
    val expiresAt: String,
    val reference: String,
    val entity: String
) : PaymentMethodResumeClientToken

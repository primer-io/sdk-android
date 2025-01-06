package io.primer.android.vouchers.retailOutlets.implementation.payment.resume.clientToken.domain.model

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeClientToken

internal data class RetailOutletsClientToken(
    override val clientTokenIntent: String,
    val expiresAt: String,
    val reference: String,
    val entity: String,
) : PaymentMethodResumeClientToken

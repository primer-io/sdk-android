package io.primer.android.nolpay.implementation.paymentCard.payment.resume.clientToken.domain.model

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeClientToken

data class NolPayClientToken(
    override val clientTokenIntent: String,
    val transactionNumber: String,
    val statusUrl: String,
    val completeUrl: String
) : PaymentMethodResumeClientToken

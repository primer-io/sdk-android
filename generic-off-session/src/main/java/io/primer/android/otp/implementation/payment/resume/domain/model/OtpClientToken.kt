package io.primer.android.otp.implementation.payment.resume.domain.model

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeClientToken

internal data class OtpClientToken(
    val statusUrl: String,
    override val clientTokenIntent: String,
) : PaymentMethodResumeClientToken

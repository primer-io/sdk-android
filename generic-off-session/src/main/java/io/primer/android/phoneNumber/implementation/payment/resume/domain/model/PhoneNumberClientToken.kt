package io.primer.android.phoneNumber.implementation.payment.resume.domain.model

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeClientToken

internal data class PhoneNumberClientToken(
    val statusUrl: String,
    override val clientTokenIntent: String,
) : PaymentMethodResumeClientToken

package io.primer.android.bancontact.implementation.payment.resume.clientToken.domain.model

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeClientToken

internal data class AdyenBancontactClientToken(
    val redirectUrl: String,
    val statusUrl: String,
    override val clientTokenIntent: String
) : PaymentMethodResumeClientToken

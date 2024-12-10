package io.primer.android.webredirect.implementation.payment.resume.clientToken.domain.model

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeClientToken

internal data class WebRedirectClientToken(
    val redirectUrl: String,
    val statusUrl: String,
    override val clientTokenIntent: String
) : PaymentMethodResumeClientToken

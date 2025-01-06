package io.primer.android.stripe.ach.implementation.payment.resume.clientToken.domain.model

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeClientToken

internal data class StripeAchClientToken(
    val sdkCompleteUrl: String?,
    val stripePaymentIntentId: String?,
    val stripeClientSecret: String?,
    override val clientTokenIntent: String,
) : PaymentMethodResumeClientToken

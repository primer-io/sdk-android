package io.primer.android.ipay88.implementation.payment.resume.clientToken.domain.model

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeClientToken

internal data class IPay88ClientToken(
    val intent: String,
    val statusUrl: String,
    val paymentId: String,
    val paymentMethod: Int,
    val actionType: String,
    val referenceNumber: String,
    val supportedCurrencyCode: String,
    val supportedCountryCode: String?,
    val backendCallbackUrl: String,
    override val clientTokenIntent: String
) : PaymentMethodResumeClientToken

package io.primer.android.qrcode.implementation.payment.resume.domain.model

import io.primer.android.paymentmethods.core.payment.resume.clientToken.domain.model.PaymentMethodResumeClientToken

internal data class QrCodeClientToken(
    override val clientTokenIntent: String,
    val statusUrl: String,
    val expiresAt: String?,
    val qrCodeUrl: String?,
    val qrCodeBase64: String
) : PaymentMethodResumeClientToken

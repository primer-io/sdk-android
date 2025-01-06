package io.primer.android.qrcode

import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo

data class QrCodeCheckoutAdditionalInfo(
    val statusUrl: String,
    val expiresAt: String?,
    val qrCodeUrl: String?,
    val qrCodeBase64: String?,
    val paymentMethodType: String,
) : PrimerCheckoutAdditionalInfo

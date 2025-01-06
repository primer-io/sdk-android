package io.primer.android.qrcode.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfiguration

internal data class QrCodeConfig(
    val paymentMethodConfigId: String,
    val locale: String,
) : PaymentMethodConfiguration

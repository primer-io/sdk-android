package io.primer.android.otp.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfiguration

internal data class OtpConfig(
    val paymentMethodConfigId: String,
    val locale: String,
) : PaymentMethodConfiguration

package io.primer.android.nolpay.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfiguration

internal data class NolPayConfig(
    val paymentMethodConfigId: String,
    val locale: String
) : PaymentMethodConfiguration

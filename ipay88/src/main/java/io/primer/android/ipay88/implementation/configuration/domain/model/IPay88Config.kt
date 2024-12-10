package io.primer.android.ipay88.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfiguration

internal data class IPay88Config(
    val paymentMethodConfigId: String,
    val locale: String
) : PaymentMethodConfiguration

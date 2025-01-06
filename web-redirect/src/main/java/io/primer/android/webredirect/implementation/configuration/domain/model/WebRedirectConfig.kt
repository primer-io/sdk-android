package io.primer.android.webredirect.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfiguration

internal data class WebRedirectConfig(
    val paymentMethodConfigId: String,
    val locale: String,
) : PaymentMethodConfiguration

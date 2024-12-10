package io.primer.android.vouchers.multibanco.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfiguration

internal data class MultibancoConfig(
    val paymentMethodConfigId: String,
    val locale: String
) : PaymentMethodConfiguration

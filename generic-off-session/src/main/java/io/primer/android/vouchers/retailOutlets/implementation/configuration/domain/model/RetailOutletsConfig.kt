package io.primer.android.vouchers.retailOutlets.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfiguration

internal data class RetailOutletsConfig(
    val paymentMethodConfigId: String,
    val locale: String,
) : PaymentMethodConfiguration

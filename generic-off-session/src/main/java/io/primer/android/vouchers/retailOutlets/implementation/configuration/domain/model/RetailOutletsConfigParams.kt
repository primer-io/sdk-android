package io.primer.android.vouchers.retailOutlets.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfigurationParams

internal data class RetailOutletsConfigParams(val paymentMethodType: String) :
    PaymentMethodConfigurationParams

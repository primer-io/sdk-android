package io.primer.android.vouchers.multibanco.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfigurationParams

internal data class MultibancoConfigParams(val paymentMethodType: String) :
    PaymentMethodConfigurationParams

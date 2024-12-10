package io.primer.android.nolpay.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfigurationParams

internal data class NolPayConfigParams(val paymentMethodType: String) :
    PaymentMethodConfigurationParams

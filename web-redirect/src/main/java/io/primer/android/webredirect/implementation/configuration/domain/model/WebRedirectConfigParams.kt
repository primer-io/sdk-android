package io.primer.android.webredirect.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfigurationParams

internal data class WebRedirectConfigParams(val paymentMethodType: String) :
    PaymentMethodConfigurationParams

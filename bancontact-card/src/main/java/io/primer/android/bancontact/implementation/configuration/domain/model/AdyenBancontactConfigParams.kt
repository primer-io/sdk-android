package io.primer.android.bancontact.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfigurationParams

internal data class AdyenBancontactConfigParams(val paymentMethodType: String) :
    PaymentMethodConfigurationParams

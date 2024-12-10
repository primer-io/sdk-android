package io.primer.android.phoneNumber.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfigurationParams

internal data class PhoneNumberConfigParams(val paymentMethodType: String) :
    PaymentMethodConfigurationParams

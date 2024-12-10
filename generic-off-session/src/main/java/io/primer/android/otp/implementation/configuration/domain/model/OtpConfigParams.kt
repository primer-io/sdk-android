package io.primer.android.otp.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfigurationParams

internal data class OtpConfigParams(val paymentMethodType: String) :
    PaymentMethodConfigurationParams

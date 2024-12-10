package io.primer.android.phoneNumber.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfiguration

internal data class PhoneNumberConfig(
    val paymentMethodConfigId: String,
    val locale: String
) : PaymentMethodConfiguration

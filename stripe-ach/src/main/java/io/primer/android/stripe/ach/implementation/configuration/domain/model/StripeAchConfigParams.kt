package io.primer.android.stripe.ach.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfigurationParams

internal data class StripeAchConfigParams(val paymentMethodType: String) :
    PaymentMethodConfigurationParams

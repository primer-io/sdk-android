package io.primer.android.paypal.implementation.configuration.domain.model

import io.primer.android.PrimerSessionIntent
import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfigurationParams

internal data class PaypalConfigParams(val sessionIntent: PrimerSessionIntent) :
    PaymentMethodConfigurationParams

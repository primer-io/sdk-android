package io.primer.android.banks.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfigurationParams

internal data class BankIssuerConfigParams(val paymentMethodType: String) :
    PaymentMethodConfigurationParams

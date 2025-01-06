package io.primer.android.banks.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfiguration
import java.util.Locale

internal data class BankIssuerConfig(
    val paymentMethodConfigId: String,
    val locale: Locale,
) : PaymentMethodConfiguration

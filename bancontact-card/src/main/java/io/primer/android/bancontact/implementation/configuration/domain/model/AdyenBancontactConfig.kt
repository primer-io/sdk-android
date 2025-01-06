package io.primer.android.bancontact.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfiguration
import java.util.Locale

internal data class AdyenBancontactConfig(
    val paymentMethodConfigId: String,
    val locale: Locale,
) : PaymentMethodConfiguration

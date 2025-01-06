package io.primer.android.stripe.ach.implementation.configuration.domain.model

import io.primer.android.paymentmethods.core.configuration.domain.model.PaymentMethodConfiguration
import java.util.Locale

internal data class StripeAchConfig(
    val paymentMethodConfigId: String,
    val locale: Locale,
) : PaymentMethodConfiguration

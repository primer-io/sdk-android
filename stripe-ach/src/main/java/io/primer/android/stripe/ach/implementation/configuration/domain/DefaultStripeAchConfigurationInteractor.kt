package io.primer.android.stripe.ach.implementation.configuration.domain

import io.primer.android.paymentmethods.core.configuration.domain.PaymentMethodConfigurationInteractor
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.stripe.ach.implementation.configuration.domain.model.StripeAchConfig
import io.primer.android.stripe.ach.implementation.configuration.domain.model.StripeAchConfigParams

internal typealias StripeAchConfigurationInteractor =
    PaymentMethodConfigurationInteractor<StripeAchConfig, StripeAchConfigParams>

internal class DefaultStripeAchConfigurationInteractor(
    configurationRepository: PaymentMethodConfigurationRepository<StripeAchConfig, StripeAchConfigParams>,
) : PaymentMethodConfigurationInteractor<StripeAchConfig, StripeAchConfigParams>(configurationRepository)

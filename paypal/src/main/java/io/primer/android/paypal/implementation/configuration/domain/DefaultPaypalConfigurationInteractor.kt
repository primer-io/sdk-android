package io.primer.android.paypal.implementation.configuration.domain

import io.primer.android.paymentmethods.core.configuration.domain.PaymentMethodConfigurationInteractor
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.paypal.implementation.configuration.domain.model.PaypalConfig
import io.primer.android.paypal.implementation.configuration.domain.model.PaypalConfigParams

internal typealias PaypalConfigurationInteractor =
    PaymentMethodConfigurationInteractor<PaypalConfig, PaypalConfigParams>

internal class DefaultPaypalConfigurationInteractor(
    configurationRepository: PaymentMethodConfigurationRepository<PaypalConfig, PaypalConfigParams>,
) : PaymentMethodConfigurationInteractor<PaypalConfig, PaypalConfigParams>(
        configurationRepository,
    )

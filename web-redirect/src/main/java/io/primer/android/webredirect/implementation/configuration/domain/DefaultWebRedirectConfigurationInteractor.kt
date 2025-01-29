package io.primer.android.webredirect.implementation.configuration.domain

import io.primer.android.paymentmethods.core.configuration.domain.PaymentMethodConfigurationInteractor
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.webredirect.implementation.configuration.domain.model.WebRedirectConfig
import io.primer.android.webredirect.implementation.configuration.domain.model.WebRedirectConfigParams

internal typealias WebRedirectConfigurationInteractor =
    PaymentMethodConfigurationInteractor<WebRedirectConfig, WebRedirectConfigParams>

internal class DefaultWebRedirectConfigurationInteractor(
    configurationRepository: PaymentMethodConfigurationRepository<WebRedirectConfig, WebRedirectConfigParams>,
) : PaymentMethodConfigurationInteractor<WebRedirectConfig, WebRedirectConfigParams>(
    configurationRepository,
)

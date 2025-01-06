package io.primer.android.ipay88.implementation.configuration.domain

import io.primer.android.ipay88.implementation.configuration.domain.model.IPay88Config
import io.primer.android.ipay88.implementation.configuration.domain.model.IPay88ConfigParams
import io.primer.android.paymentmethods.core.configuration.domain.PaymentMethodConfigurationInteractor
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository

internal typealias IPay88ConfigurationInteractor =
    PaymentMethodConfigurationInteractor<IPay88Config, IPay88ConfigParams>

internal class DefaultIPay88ConfigurationInteractor(
    configurationRepository: PaymentMethodConfigurationRepository<IPay88Config, IPay88ConfigParams>,
) : PaymentMethodConfigurationInteractor<IPay88Config, IPay88ConfigParams>(
        configurationRepository,
    )

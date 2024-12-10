package io.primer.android.nolpay.implementation.configuration.domain

import io.primer.android.paymentmethods.core.configuration.domain.PaymentMethodConfigurationInteractor
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.nolpay.implementation.configuration.domain.model.NolPayConfig
import io.primer.android.nolpay.implementation.configuration.domain.model.NolPayConfigParams

internal typealias NolPayConfigurationInteractor =
    PaymentMethodConfigurationInteractor<NolPayConfig, NolPayConfigParams>

internal class DefaultNolPayConfigurationInteractor(
    configurationRepository: PaymentMethodConfigurationRepository<NolPayConfig, NolPayConfigParams>
) : PaymentMethodConfigurationInteractor<NolPayConfig, NolPayConfigParams>(
    configurationRepository
)

package io.primer.android.vouchers.multibanco.implementation.configuration.domain

import io.primer.android.paymentmethods.core.configuration.domain.PaymentMethodConfigurationInteractor
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.vouchers.multibanco.implementation.configuration.domain.model.MultibancoConfig
import io.primer.android.vouchers.multibanco.implementation.configuration.domain.model.MultibancoConfigParams

internal typealias MultibancoConfigurationInteractor =
    PaymentMethodConfigurationInteractor<MultibancoConfig, MultibancoConfigParams>

internal class DefaultMultibancoConfigurationInteractor(
    configurationRepository: PaymentMethodConfigurationRepository<MultibancoConfig, MultibancoConfigParams>,
) : PaymentMethodConfigurationInteractor<MultibancoConfig, MultibancoConfigParams>(
    configurationRepository,
)

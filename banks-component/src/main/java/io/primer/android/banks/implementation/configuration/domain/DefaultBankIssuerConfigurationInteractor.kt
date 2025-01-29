package io.primer.android.banks.implementation.configuration.domain

import io.primer.android.banks.implementation.configuration.domain.model.BankIssuerConfig
import io.primer.android.banks.implementation.configuration.domain.model.BankIssuerConfigParams
import io.primer.android.paymentmethods.core.configuration.domain.PaymentMethodConfigurationInteractor
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository

internal typealias BankIssuerConfigurationInteractor =
    PaymentMethodConfigurationInteractor<BankIssuerConfig, BankIssuerConfigParams>

internal class DefaultBankIssuerConfigurationInteractor(
    configurationRepository: PaymentMethodConfigurationRepository<BankIssuerConfig, BankIssuerConfigParams>,
) : PaymentMethodConfigurationInteractor<BankIssuerConfig, BankIssuerConfigParams>(
    configurationRepository,
)

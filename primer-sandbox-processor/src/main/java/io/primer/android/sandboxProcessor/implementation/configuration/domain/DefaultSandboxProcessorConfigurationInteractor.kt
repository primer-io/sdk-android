package io.primer.android.sandboxProcessor.implementation.configuration.domain

import io.primer.android.paymentmethods.core.configuration.domain.PaymentMethodConfigurationInteractor
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.sandboxProcessor.implementation.configuration.domain.model.SandboxProcessorConfig
import io.primer.android.sandboxProcessor.implementation.configuration.domain.model.SandboxProcessorConfigParams

internal typealias ProcessorTestConfigurationInteractor =
    PaymentMethodConfigurationInteractor<SandboxProcessorConfig, SandboxProcessorConfigParams>

internal class DefaultSandboxProcessorConfigurationInteractor(
    configurationRepository: PaymentMethodConfigurationRepository<SandboxProcessorConfig, SandboxProcessorConfigParams>,
) : PaymentMethodConfigurationInteractor<SandboxProcessorConfig, SandboxProcessorConfigParams>(
        configurationRepository,
    )

package io.primer.android.sandboxProcessor.implementation.configuration.data.repository

import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.paymentmethods.core.errors.data.exception.AsyncIllegalValueKey
import io.primer.android.sandboxProcessor.implementation.configuration.domain.model.SandboxProcessorConfig
import io.primer.android.sandboxProcessor.implementation.configuration.domain.model.SandboxProcessorConfigParams

internal class SandboxProcessorConfigurationDataRepository(
    private val configurationDataSource: CacheConfigurationDataSource,
    private val settings: PrimerSettings,
) : PaymentMethodConfigurationRepository<SandboxProcessorConfig, SandboxProcessorConfigParams> {
    override fun getPaymentMethodConfiguration(params: SandboxProcessorConfigParams) =
        runSuspendCatching {
            SandboxProcessorConfig(
                paymentMethodConfigId =
                requireNotNullCheck(
                    configurationDataSource.get()
                        .paymentMethods.firstOrNull { it.type == params.paymentMethodType }?.id,
                    AsyncIllegalValueKey.PAYMENT_METHOD_CONFIG_ID,
                ),
                locale = settings.locale.toLanguageTag(),
            )
        }
}

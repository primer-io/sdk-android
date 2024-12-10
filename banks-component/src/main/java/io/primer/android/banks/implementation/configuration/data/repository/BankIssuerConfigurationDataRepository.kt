package io.primer.android.banks.implementation.configuration.data.repository

import io.primer.android.data.settings.PrimerSettings
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.banks.implementation.configuration.domain.model.BankIssuerConfig
import io.primer.android.banks.implementation.configuration.domain.model.BankIssuerConfigParams
import io.primer.android.paymentmethods.core.errors.data.exception.AsyncIllegalValueKey
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.errors.utils.requireNotNullCheck

internal class BankIssuerConfigurationDataRepository(
    private val configurationDataSource: CacheConfigurationDataSource,
    private val settings: PrimerSettings
) : PaymentMethodConfigurationRepository<BankIssuerConfig, BankIssuerConfigParams> {

    override fun getPaymentMethodConfiguration(params: BankIssuerConfigParams) =
        runSuspendCatching {
            BankIssuerConfig(
                paymentMethodConfigId = requireNotNullCheck(
                    configurationDataSource.get()
                        .paymentMethods.firstOrNull { it.type == params.paymentMethodType }?.id,
                    AsyncIllegalValueKey.PAYMENT_METHOD_CONFIG_ID
                ),
                locale = settings.locale
            )
        }
}

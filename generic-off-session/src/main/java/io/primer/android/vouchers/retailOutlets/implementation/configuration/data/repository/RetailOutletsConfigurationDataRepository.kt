package io.primer.android.vouchers.retailOutlets.implementation.configuration.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.paymentmethods.core.errors.data.exception.AsyncIllegalValueKey
import io.primer.android.vouchers.retailOutlets.implementation.configuration.domain.model.RetailOutletsConfig
import io.primer.android.vouchers.retailOutlets.implementation.configuration.domain.model.RetailOutletsConfigParams

internal class RetailOutletsConfigurationDataRepository(
    private val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    private val settings: PrimerSettings,
) : PaymentMethodConfigurationRepository<RetailOutletsConfig, RetailOutletsConfigParams> {
    override fun getPaymentMethodConfiguration(params: RetailOutletsConfigParams) =
        runSuspendCatching {
            RetailOutletsConfig(
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

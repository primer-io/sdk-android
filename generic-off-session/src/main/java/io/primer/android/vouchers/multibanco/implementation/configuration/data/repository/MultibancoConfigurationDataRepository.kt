package io.primer.android.vouchers.multibanco.implementation.configuration.data.repository

import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.paymentmethods.core.errors.data.exception.AsyncIllegalValueKey
import io.primer.android.vouchers.multibanco.implementation.configuration.domain.model.MultibancoConfig
import io.primer.android.vouchers.multibanco.implementation.configuration.domain.model.MultibancoConfigParams

internal class MultibancoConfigurationDataRepository(
    private val configurationDataSource: CacheConfigurationDataSource,
    private val settings: PrimerSettings,
) : PaymentMethodConfigurationRepository<MultibancoConfig, MultibancoConfigParams> {
    override fun getPaymentMethodConfiguration(params: MultibancoConfigParams) =
        runSuspendCatching {
            MultibancoConfig(
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

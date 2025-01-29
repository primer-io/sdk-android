package io.primer.android.ipay88.implementation.configuration.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.ipay88.implementation.configuration.domain.model.IPay88Config
import io.primer.android.ipay88.implementation.configuration.domain.model.IPay88ConfigParams
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.paymentmethods.core.errors.data.exception.AsyncIllegalValueKey

internal class IPay88ConfigurationDataRepository(
    private val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    private val settings: PrimerSettings,
) : PaymentMethodConfigurationRepository<IPay88Config, IPay88ConfigParams> {
    override fun getPaymentMethodConfiguration(params: IPay88ConfigParams) =
        runSuspendCatching {
            IPay88Config(
                paymentMethodConfigId =
                requireNotNullCheck(
                    configurationDataSource.get()
                        .paymentMethods.first { it.type == params.paymentMethodType }.id,
                    AsyncIllegalValueKey.PAYMENT_METHOD_CONFIG_ID,
                ),
                locale = settings.locale.toLanguageTag(),
            )
        }
}

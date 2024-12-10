package io.primer.android.webredirect.implementation.configuration.data.repository

import io.primer.android.data.settings.PrimerSettings
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.webredirect.implementation.configuration.domain.model.WebRedirectConfig
import io.primer.android.webredirect.implementation.configuration.domain.model.WebRedirectConfigParams
import io.primer.android.paymentmethods.core.errors.data.exception.AsyncIllegalValueKey
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.errors.utils.requireNotNullCheck

internal class WebRedirectConfigurationDataRepository(
    private val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    private val settings: PrimerSettings
) : PaymentMethodConfigurationRepository<WebRedirectConfig, WebRedirectConfigParams> {

    override fun getPaymentMethodConfiguration(params: WebRedirectConfigParams) =
        runSuspendCatching {
            WebRedirectConfig(
                paymentMethodConfigId = requireNotNullCheck(
                    configurationDataSource.get()
                        .paymentMethods.first { it.type == params.paymentMethodType }.id,
                    AsyncIllegalValueKey.PAYMENT_METHOD_CONFIG_ID
                ),
                locale = settings.locale.toLanguageTag()
            )
        }
}

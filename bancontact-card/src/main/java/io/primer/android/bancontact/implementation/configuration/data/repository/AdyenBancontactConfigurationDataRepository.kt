package io.primer.android.bancontact.implementation.configuration.data.repository

import io.primer.android.data.settings.PrimerSettings
import io.primer.android.bancontact.implementation.configuration.domain.model.AdyenBancontactConfig
import io.primer.android.bancontact.implementation.configuration.domain.model.AdyenBancontactConfigParams
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.paymentmethods.core.errors.data.exception.AsyncIllegalValueKey

internal class AdyenBancontactConfigurationDataRepository(
    private val configurationDataSource: CacheConfigurationDataSource,
    private val settings: PrimerSettings
) : PaymentMethodConfigurationRepository<AdyenBancontactConfig, AdyenBancontactConfigParams> {

    override fun getPaymentMethodConfiguration(params: AdyenBancontactConfigParams) =
        runSuspendCatching {
            AdyenBancontactConfig(
                paymentMethodConfigId = requireNotNullCheck(
                    configurationDataSource.get()
                        .paymentMethods.firstOrNull { it.type == params.paymentMethodType }?.id,
                    AsyncIllegalValueKey.PAYMENT_METHOD_CONFIG_ID
                ),
                locale = settings.locale
            )
        }
}

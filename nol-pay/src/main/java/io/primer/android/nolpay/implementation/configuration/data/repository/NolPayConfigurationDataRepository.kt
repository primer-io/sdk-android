package io.primer.android.nolpay.implementation.configuration.data.repository

import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.nolpay.implementation.configuration.domain.model.NolPayConfig
import io.primer.android.nolpay.implementation.configuration.domain.model.NolPayConfigParams
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.paymentmethods.core.errors.data.exception.AsyncIllegalValueKey

internal class NolPayConfigurationDataRepository(
    private val configurationDataSource: CacheConfigurationDataSource,
    private val primerSettings: PrimerSettings,
) : PaymentMethodConfigurationRepository<NolPayConfig, NolPayConfigParams> {
    override fun getPaymentMethodConfiguration(params: NolPayConfigParams) =
        runSuspendCatching {
            NolPayConfig(
                paymentMethodConfigId =
                    requireNotNullCheck(
                        configurationDataSource.get()
                            .paymentMethods.first { it.type == params.paymentMethodType }.id,
                        AsyncIllegalValueKey.PAYMENT_METHOD_CONFIG_ID,
                    ),
                locale = primerSettings.locale.toLanguageTag(),
            )
        }
}

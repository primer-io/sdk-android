package io.primer.android.otp.implementation.configuration.data.repository

import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.otp.implementation.configuration.domain.model.OtpConfig
import io.primer.android.otp.implementation.configuration.domain.model.OtpConfigParams
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.paymentmethods.core.errors.data.exception.AsyncIllegalValueKey

internal class OtpConfigurationDataRepository(
    private val configurationDataSource: CacheConfigurationDataSource,
    private val settings: PrimerSettings,
) : PaymentMethodConfigurationRepository<OtpConfig, OtpConfigParams> {
    override fun getPaymentMethodConfiguration(params: OtpConfigParams) =
        runSuspendCatching {
            OtpConfig(
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

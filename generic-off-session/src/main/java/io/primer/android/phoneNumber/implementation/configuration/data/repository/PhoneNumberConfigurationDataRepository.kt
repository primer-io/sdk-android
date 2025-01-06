package io.primer.android.phoneNumber.implementation.configuration.data.repository

import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.data.settings.PrimerSettings
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.paymentmethods.core.errors.data.exception.AsyncIllegalValueKey
import io.primer.android.phoneNumber.implementation.configuration.domain.model.PhoneNumberConfig
import io.primer.android.phoneNumber.implementation.configuration.domain.model.PhoneNumberConfigParams

internal class PhoneNumberConfigurationDataRepository(
    private val configurationDataSource: CacheConfigurationDataSource,
    private val settings: PrimerSettings,
) : PaymentMethodConfigurationRepository<PhoneNumberConfig, PhoneNumberConfigParams> {
    override fun getPaymentMethodConfiguration(params: PhoneNumberConfigParams) =
        runSuspendCatching {
            PhoneNumberConfig(
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

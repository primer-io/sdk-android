package io.primer.android.stripe.ach.implementation.configuration.data.repository

import io.primer.android.data.settings.PrimerSettings
import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.paymentmethods.core.errors.data.exception.AsyncIllegalValueKey
import io.primer.android.stripe.ach.implementation.configuration.domain.model.StripeAchConfig
import io.primer.android.stripe.ach.implementation.configuration.domain.model.StripeAchConfigParams

internal class StripeAchConfigurationDataRepository(
    private val configurationDataSource: CacheConfigurationDataSource,
    private val settings: PrimerSettings
) : PaymentMethodConfigurationRepository<StripeAchConfig, StripeAchConfigParams> {

    override fun getPaymentMethodConfiguration(params: StripeAchConfigParams) =
        runSuspendCatching {
            StripeAchConfig(
                paymentMethodConfigId = requireNotNullCheck(
                    configurationDataSource.get()
                        .paymentMethods.firstOrNull { it.type == params.paymentMethodType }?.id,
                    AsyncIllegalValueKey.PAYMENT_METHOD_CONFIG_ID
                ),
                locale = settings.locale
            )
        }
}

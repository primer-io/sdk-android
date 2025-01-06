package io.primer.android.nolpay.implementation.common.data.repository

import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.nolpay.implementation.common.domain.model.NolPayConfiguration
import io.primer.android.nolpay.implementation.common.domain.repository.NolPaySdkInitConfigurationRepository
import io.primer.android.nolpay.implementation.errors.data.exception.NolPayIllegalValueKey
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType

internal class NolPaySdkInitSdkInitConfigurationDataRepository(
    private val configurationDataSource: CacheConfigurationDataSource,
) : NolPaySdkInitConfigurationRepository {
    override suspend fun getConfiguration() =
        runSuspendCatching {
            val paymentMethodConfig =
                configurationDataSource.get().paymentMethods
                    .first { it.type == PaymentMethodType.NOL_PAY.name }

            NolPayConfiguration(
                requireNotNullCheck(
                    paymentMethodConfig.options?.merchantAppId,
                    NolPayIllegalValueKey.MERCHANT_APP_ID,
                ),
                configurationDataSource.get().environment,
            )
        }
}

package io.primer.android.components.data.payments.paymentMethods.nolpay.repository

import io.primer.android.components.data.payments.paymentMethods.nolpay.exception.NolPayIllegalValueKey
import io.primer.android.components.domain.payments.paymentMethods.nolpay.models.NolPayConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nolpay.repository.NolPayConfigurationRepository
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.extensions.runSuspendCatching

internal class NolPayConfigurationDataRepository(
    private val localConfigurationDataSource: LocalConfigurationDataSource
) : NolPayConfigurationRepository {
    override suspend fun getConfiguration() = runSuspendCatching {
        val paymentMethodConfig =
            localConfigurationDataSource.getConfiguration().paymentMethods
                .first { it.type == PaymentMethodType.NOL_PAY.name }

        NolPayConfiguration(
            requireNotNullCheck(
                paymentMethodConfig.options?.merchantAppId,
                NolPayIllegalValueKey.MERCHANT_APP_ID
            ),
            localConfigurationDataSource.getConfiguration().environment
        )
    }
}

package io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.repository

import io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.exception.ApayaIllegalValueKey
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.models.ApayaTokenizationConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.repository.ApayaTokenizationConfigurationRepository
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.PrimerSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class ApayaTokenizationConfigurationDataRepository(
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val settings: PrimerSettings
) : ApayaTokenizationConfigurationRepository {
    override fun getConfiguration(): Flow<ApayaTokenizationConfiguration> {
        return flow {
            val paymentMethodConfig =
                localConfigurationDataSource.getConfiguration().paymentMethods
                    .first { it.type == PaymentMethodType.APAYA.name }
            emit(
                ApayaTokenizationConfiguration(
                    requireNotNullCheck(
                        paymentMethodConfig.options?.merchantId,
                        ApayaIllegalValueKey.MERCHANT_ID
                    ),
                    settings.currency,
                )
            )
        }
    }
}

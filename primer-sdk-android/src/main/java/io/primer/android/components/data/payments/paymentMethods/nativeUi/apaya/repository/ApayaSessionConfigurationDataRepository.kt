package io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.repository

import io.primer.android.components.data.payments.paymentMethods.nativeUi.apaya.exception.ApayaIllegalValueKey
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.models.ApayaSessionConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.apaya.repository.ApayaSessionConfigurationRepository
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.settings.PrimerSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class ApayaSessionConfigurationDataRepository(
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val settings: PrimerSettings
) : ApayaSessionConfigurationRepository {
    override fun getConfiguration(): Flow<ApayaSessionConfiguration> {
        return flow {
            val paymentMethodConfig =
                localConfigurationDataSource.getConfiguration().paymentMethods
                    .first { it.type == PaymentMethodType.APAYA.name }
            emit(
                ApayaSessionConfiguration(
                    requireNotNullCheck(
                        paymentMethodConfig.options?.merchantAccountId,
                        ApayaIllegalValueKey.MERCHANT_ACCOUNT_ID
                    ),
                    settings.locale,
                    settings.currency,
                    requireNotNullCheck(
                        settings.customer.mobileNumber,
                        ApayaIllegalValueKey.CUSTOMER_MOBILE_NUMBER
                    ),
                )
            )
        }
    }
}

package io.primer.android.components.data.payments.paymentMethods.nativeUi.async.redirect.repository

import io.primer.android.components.data.payments.paymentMethods.nativeUi.async.redirect.exception.AsyncIllegalValueKey
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.async.redirect.models.AsyncPaymentMethodConfig
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.async.redirect.repository.AsyncPaymentMethodRepository
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.settings.internal.PrimerConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class AsyncPaymentMethodDataRepository(
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val config: PrimerConfig
) : AsyncPaymentMethodRepository {
    override fun getPaymentMethodConfig(paymentMethodType: String): Flow<AsyncPaymentMethodConfig> {
        return flow {
            emit(
                AsyncPaymentMethodConfig(
                    requireNotNullCheck(
                        localConfigurationDataSource.getConfiguration()
                            .paymentMethods.first { it.type == paymentMethodType }.id,
                        AsyncIllegalValueKey.PAYMENT_METHOD_CONFIG_ID
                    ),
                    config.settings.locale.toLanguageTag()
                )
            )
        }
    }
}

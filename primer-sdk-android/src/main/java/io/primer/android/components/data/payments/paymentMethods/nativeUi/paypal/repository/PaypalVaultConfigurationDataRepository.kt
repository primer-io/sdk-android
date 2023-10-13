package io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.repository

import android.net.Uri
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.exception.PaypalIllegalValueKey
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalVaultConfiguration
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository.PaypalVaultConfigurationRepository
import io.primer.android.data.base.util.requireNotNullCheck
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.infrastructure.metadata.datasource.MetaDataSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.UUID

internal class PaypalVaultConfigurationDataRepository(
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val metaDataSource: MetaDataSource
) : PaypalVaultConfigurationRepository {
    override fun getPaypalConfiguration(): Flow<PaypalVaultConfiguration> {
        return flow {
            val paymentMethodConfig =
                localConfigurationDataSource.getConfiguration().paymentMethods
                    .first { it.type == PaymentMethodType.PAYPAL.name }
            val uuid = UUID.randomUUID().toString()
            val host = "$PRIMER_PAYPAL_PREFIX${metaDataSource.getApplicationId()}"
            emit(
                PaypalVaultConfiguration(
                    requireNotNullCheck(
                        paymentMethodConfig.id,
                        PaypalIllegalValueKey.PAYMENT_METHOD_CONFIG_ID
                    ),
                    Uri.Builder().scheme(PRIMER_PAYPAL_SCHEMA)
                        .authority(host)
                        .appendPath(PRIMER_PAYPAL_PATH_PREFIX)
                        .appendPath(uuid)
                        .appendPath(SUCCESS_PATH_SEGMENT)
                        .build()
                        .toString(),
                    Uri.Builder().scheme(PRIMER_PAYPAL_SCHEMA)
                        .authority(host)
                        .appendPath(PRIMER_PAYPAL_PATH_PREFIX)
                        .appendPath(uuid)
                        .appendPath(CANCEL_PATH_SEGMENT)
                        .build()
                        .toString()
                )
            )
        }
    }

    private companion object {

        const val PRIMER_PAYPAL_SCHEMA = "primer"
        const val PRIMER_PAYPAL_PREFIX = "requestor."
        const val PRIMER_PAYPAL_PATH_PREFIX = "paypal"
        const val SUCCESS_PATH_SEGMENT = "success"
        const val CANCEL_PATH_SEGMENT = "cancel"
    }
}

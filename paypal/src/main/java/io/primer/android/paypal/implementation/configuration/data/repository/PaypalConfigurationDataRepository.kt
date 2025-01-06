package io.primer.android.paypal.implementation.configuration.data.repository

import android.net.Uri
import io.primer.android.PrimerSessionIntent
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.core.utils.BaseDataProvider
import io.primer.android.errors.utils.requireNotNullCheck
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paymentmethods.core.configuration.domain.repository.PaymentMethodConfigurationRepository
import io.primer.android.paypal.implementation.configuration.domain.model.PaypalConfig
import io.primer.android.paypal.implementation.configuration.domain.model.PaypalConfigParams
import io.primer.android.paypal.implementation.errors.data.exception.PaypalIllegalValueKey
import java.util.UUID

internal class PaypalConfigurationDataRepository(
    private val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    private val applicationIdProvider: BaseDataProvider<String>,
) : PaymentMethodConfigurationRepository<PaypalConfig, PaypalConfigParams> {
    override fun getPaymentMethodConfiguration(params: PaypalConfigParams) =
        runSuspendCatching {
            val paymentMethodConfig =
                configurationDataSource.get().paymentMethods
                    .first { it.type == PaymentMethodType.PAYPAL.name }

            val uuid = UUID.randomUUID().toString()
            val host = "$PRIMER_PAYPAL_PREFIX${applicationIdProvider.provide()}"
            val baseUri =
                Uri.Builder()
                    .scheme(PRIMER_PAYPAL_SCHEMA)
                    .authority(host)
                    .appendPath(PRIMER_PAYPAL_PATH_PREFIX)
                    .appendPath(uuid)

            when (params.sessionIntent) {
                PrimerSessionIntent.CHECKOUT -> {
                    val order = configurationDataSource.get().clientSession.order

                    PaypalConfig.PaypalCheckoutConfiguration(
                        paymentMethodConfigId =
                            requireNotNullCheck(
                                paymentMethodConfig.id,
                                PaypalIllegalValueKey.PAYMENT_METHOD_CONFIG_ID,
                            ),
                        amount = order?.currentAmount,
                        currencyCode = order?.currencyCode,
                        successUrl =
                            baseUri
                                .appendPath(SUCCESS_PATH_SEGMENT)
                                .build()
                                .toString(),
                        cancelUrl =
                            baseUri
                                .appendPath(CANCEL_PATH_SEGMENT)
                                .build()
                                .toString(),
                    )
                }

                PrimerSessionIntent.VAULT -> {
                    PaypalConfig.PaypalVaultConfiguration(
                        paymentMethodConfigId =
                            requireNotNullCheck(
                                paymentMethodConfig.id,
                                PaypalIllegalValueKey.PAYMENT_METHOD_CONFIG_ID,
                            ),
                        successUrl =
                            baseUri
                                .appendPath(SUCCESS_PATH_SEGMENT)
                                .build()
                                .toString(),
                        cancelUrl =
                            baseUri
                                .appendPath(CANCEL_PATH_SEGMENT)
                                .build()
                                .toString(),
                    )
                }
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

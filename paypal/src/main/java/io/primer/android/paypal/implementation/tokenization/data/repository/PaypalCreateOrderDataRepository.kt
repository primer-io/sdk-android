package io.primer.android.paypal.implementation.tokenization.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.exception.HttpException
import io.primer.android.core.extensions.onError
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.errors.data.exception.SessionCreateException
import io.primer.android.paymentmethods.common.data.model.PaymentMethodType
import io.primer.android.paypal.implementation.tokenization.data.datasource.RemotePaypalCreateOrderDataSource
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalCreateOrderDataRequest
import io.primer.android.paypal.implementation.tokenization.data.model.toPaypalOrder
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalCreateOrderParams
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrder
import io.primer.android.paypal.implementation.tokenization.domain.repository.PaypalCreateOrderRepository

internal class PaypalCreateOrderDataRepository(
    private val createOrderDataSource: RemotePaypalCreateOrderDataSource,
    private val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
) : PaypalCreateOrderRepository {
    override suspend fun createOrder(params: PaypalCreateOrderParams): Result<PaypalOrder> =
        runSuspendCatching {
            createOrderDataSource.execute(
                BaseRemoteHostRequest(
                    configurationDataSource.get().coreUrl,
                    PaypalCreateOrderDataRequest(
                        paymentMethodConfigId = params.paymentMethodConfigId,
                        amount = params.amount,
                        currencyCode = params.currencyCode,
                        returnUrl = params.successUrl,
                        cancelUrl = params.cancelUrl,
                    ),
                ),
            ).toPaypalOrder(
                successUrl = params.successUrl,
                cancelUrl = params.cancelUrl,
            )
        }.onError {
            when {
                it is HttpException && it.isClientError() ->
                    throw SessionCreateException(
                        PaymentMethodType.PAYPAL.name,
                        it.error.diagnosticsId,
                        it.error.description,
                    )

                else -> throw it
            }
        }
}

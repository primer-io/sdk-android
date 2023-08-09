package io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.repository

import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.datasource.RemotePaypalCreateOrderDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalCreateOrderDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.toPaypalOrder
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalCreateOrderParams
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalOrder
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository.PaypalCreateOrderRepository
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.SessionCreateException
import io.primer.android.extensions.doOnError
import io.primer.android.http.exception.HttpException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class PaypalCreateOrderDataRepository(
    private val createOrderDataSource: RemotePaypalCreateOrderDataSource,
    private val localConfigurationDataSource: LocalConfigurationDataSource,
) : PaypalCreateOrderRepository {
    override fun createOrder(params: PaypalCreateOrderParams): Flow<PaypalOrder> {
        return createOrderDataSource.execute(
            BaseRemoteRequest(
                localConfigurationDataSource.getConfiguration(),
                PaypalCreateOrderDataRequest(
                    params.paymentMethodConfigId,
                    params.amount,
                    params.currencyCode,
                    params.successUrl,
                    params.cancelUrl,
                )
            )
        ).map { it.toPaypalOrder(params.successUrl, params.cancelUrl) }
            .doOnError {
                when {
                    it is HttpException && it.isClientError() ->
                        throw SessionCreateException(
                            PaymentMethodType.PAYPAL,
                            it.error.diagnosticsId,
                            it.error.description
                        )
                    else -> throw it
                }
            }
    }
}

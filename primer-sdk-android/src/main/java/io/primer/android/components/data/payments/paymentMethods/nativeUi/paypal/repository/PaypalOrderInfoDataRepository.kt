package io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.repository

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalOrderInfoParams
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.repository.PaypalInfoRepository
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.datasource.RemotePaypalOrderInfoDataSource
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.toPaypalOrder
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.toPaypalOrderInfoRequest
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest

internal class PaypalOrderInfoDataRepository(
    private val remotePaypalOrderInfoDataSource: RemotePaypalOrderInfoDataSource,
    private var configurationDataSource: LocalConfigurationDataSource
) : PaypalInfoRepository {

    override fun getPaypalOrderInfo(params: PaypalOrderInfoParams) = configurationDataSource.get()
        .flatMapLatest {
            remotePaypalOrderInfoDataSource.execute(
                BaseRemoteRequest(
                    it,
                    params.toPaypalOrderInfoRequest()
                )
            ).mapLatest {
                it.toPaypalOrder()
            }
        }
}

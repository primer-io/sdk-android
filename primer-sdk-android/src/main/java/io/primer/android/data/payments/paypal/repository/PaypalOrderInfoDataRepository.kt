package io.primer.android.data.payments.paypal.repository

import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.payments.paypal.datasource.RemotePaypalOrderInfoDataSource
import io.primer.android.data.payments.paypal.models.toPaypalOrder
import io.primer.android.data.payments.paypal.models.toPaypalOrderInfoRequest
import io.primer.android.domain.payments.paypal.models.PaypalOrderInfoParams
import io.primer.android.domain.payments.paypal.repository.PaypalInfoRepository
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest

internal class PaypalOrderInfoDataRepository(
    private val remotePaypalOrderInfoDataSource: RemotePaypalOrderInfoDataSource,
    private var configurationDataSource: LocalConfigurationDataSource,
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

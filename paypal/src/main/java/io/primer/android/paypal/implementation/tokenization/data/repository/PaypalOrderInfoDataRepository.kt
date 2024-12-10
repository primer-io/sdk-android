package io.primer.android.paypal.implementation.tokenization.data.repository

import io.primer.android.paypal.implementation.tokenization.data.datasource.RemotePaypalOrderInfoDataSource
import io.primer.android.paypal.implementation.tokenization.data.model.toPaypalOrder
import io.primer.android.paypal.implementation.tokenization.data.model.toPaypalOrderInfoRequest
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrderInfoParams
import io.primer.android.paypal.implementation.tokenization.domain.repository.PaypalInfoRepository

internal class PaypalOrderInfoDataRepository(
    private val remotePaypalOrderInfoDataSource: RemotePaypalOrderInfoDataSource,
    private val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>
) : PaypalInfoRepository {

    override suspend fun getPaypalOrderInfo(params: PaypalOrderInfoParams) = runSuspendCatching {
        configurationDataSource.get()
            .let {
                remotePaypalOrderInfoDataSource.execute(
                    BaseRemoteHostRequest(
                        host = it.coreUrl,
                        data = params.toPaypalOrderInfoRequest()
                    )
                ).toPaypalOrder()
            }
    }
}

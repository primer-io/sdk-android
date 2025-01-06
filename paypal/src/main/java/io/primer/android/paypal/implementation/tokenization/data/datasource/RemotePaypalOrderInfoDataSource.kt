package io.primer.android.paypal.implementation.tokenization.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalOrderInfoDataRequest
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalOrderInfoResponse

internal class RemotePaypalOrderInfoDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseSuspendDataSource<PaypalOrderInfoResponse, BaseRemoteHostRequest<PaypalOrderInfoDataRequest>> {
    override suspend fun execute(input: BaseRemoteHostRequest<PaypalOrderInfoDataRequest>) =
        primerHttpClient.suspendPost<PaypalOrderInfoDataRequest, PaypalOrderInfoResponse>(
            url = "${input.host}/paypal/orders",
            request = input.data,
        ).body
}

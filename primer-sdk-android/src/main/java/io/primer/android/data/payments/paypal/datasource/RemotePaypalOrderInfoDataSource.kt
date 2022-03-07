package io.primer.android.data.payments.paypal.datasource

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.payments.paypal.models.PaypalOrderInfoRequest
import io.primer.android.data.payments.paypal.models.PaypalOrderInfoResponse
import io.primer.android.http.PrimerHttpClient

internal class RemotePaypalOrderInfoDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseFlowDataSource<PaypalOrderInfoResponse, BaseRemoteRequest<PaypalOrderInfoRequest>> {

    override fun execute(input: BaseRemoteRequest<PaypalOrderInfoRequest>) =
        primerHttpClient.post<PaypalOrderInfoRequest, PaypalOrderInfoResponse>(
            "${input.configuration.coreUrl}/paypal/orders",
            input.data
        )
}

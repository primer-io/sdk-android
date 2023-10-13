package io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.datasource

import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalOrderInfoDataRequest
import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.models.PaypalOrderInfoResponse
import io.primer.android.http.PrimerHttpClient

internal class RemotePaypalOrderInfoDataSource(
    private val primerHttpClient: PrimerHttpClient
) : BaseFlowDataSource<PaypalOrderInfoResponse, BaseRemoteRequest<PaypalOrderInfoDataRequest>> {

    override fun execute(input: BaseRemoteRequest<PaypalOrderInfoDataRequest>) =
        primerHttpClient.post<PaypalOrderInfoDataRequest, PaypalOrderInfoResponse>(
            "${input.configuration.coreUrl}/paypal/orders",
            input.data
        )
}

package io.primer.android.paypal.implementation.tokenization.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalCreateOrderDataRequest
import io.primer.android.paypal.implementation.tokenization.data.model.PaypalCreateOrderDataResponse

internal class RemotePaypalCreateOrderDataSource(private val primerHttpClient: PrimerHttpClient) :
    BaseSuspendDataSource<
        PaypalCreateOrderDataResponse,
        BaseRemoteHostRequest<PaypalCreateOrderDataRequest>,
        > {
    override suspend fun execute(
        input: BaseRemoteHostRequest<PaypalCreateOrderDataRequest>,
    ): PaypalCreateOrderDataResponse {
        return primerHttpClient.suspendPost<PaypalCreateOrderDataRequest, PaypalCreateOrderDataResponse>(
            url = "${input.host}/paypal/orders/create",
            request = input.data,
        ).body
    }
}

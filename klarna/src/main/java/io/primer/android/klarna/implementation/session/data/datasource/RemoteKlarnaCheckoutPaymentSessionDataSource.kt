package io.primer.android.klarna.implementation.session.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.klarna.implementation.session.data.models.CreateCheckoutPaymentSessionDataRequest
import io.primer.android.klarna.implementation.session.data.models.CreateSessionDataResponse

internal class RemoteKlarnaCheckoutPaymentSessionDataSource(
    private val primerHttpClient: PrimerHttpClient
) : BaseSuspendDataSource<CreateSessionDataResponse,
        BaseRemoteHostRequest<CreateCheckoutPaymentSessionDataRequest>> {
    override suspend fun execute(input: BaseRemoteHostRequest<CreateCheckoutPaymentSessionDataRequest>):
        CreateSessionDataResponse = primerHttpClient.suspendPost<
        CreateCheckoutPaymentSessionDataRequest, CreateSessionDataResponse
        >(
        "${input.host}/klarna/payment-sessions",
        input.data
    ).body
}

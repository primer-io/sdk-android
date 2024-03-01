package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.datasource

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateCheckoutPaymentSessionDataRequest
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateSessionDataResponse
import io.primer.android.data.base.datasource.BaseSuspendDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.http.PrimerHttpClient

internal class RemoteKlarnaCheckoutPaymentSessionDataSource(
    private val primerHttpClient: PrimerHttpClient
) : BaseSuspendDataSource<CreateSessionDataResponse,
        BaseRemoteRequest<CreateCheckoutPaymentSessionDataRequest>> {
    override suspend fun execute(input: BaseRemoteRequest<CreateCheckoutPaymentSessionDataRequest>):
        CreateSessionDataResponse = primerHttpClient.postSuspend(
        "${input.configuration.coreUrl}/klarna/payment-sessions",
        input.data
    )
}

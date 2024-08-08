package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.datasource

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateSessionDataResponse
import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateVaultPaymentSessionDataRequest
import io.primer.android.data.base.datasource.BaseSuspendDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.http.PrimerHttpClient

internal class RemoteKlarnaVaultPaymentSessionDataSource(
    private val primerHttpClient: PrimerHttpClient
) : BaseSuspendDataSource<CreateSessionDataResponse,
        BaseRemoteRequest<CreateVaultPaymentSessionDataRequest>> {
    override suspend fun execute(input: BaseRemoteRequest<CreateVaultPaymentSessionDataRequest>):
        CreateSessionDataResponse = primerHttpClient.postSuspend<
        CreateVaultPaymentSessionDataRequest, CreateSessionDataResponse
        >(
        "${input.configuration.coreUrl}/klarna/payment-sessions",
        input.data
    ).body
}

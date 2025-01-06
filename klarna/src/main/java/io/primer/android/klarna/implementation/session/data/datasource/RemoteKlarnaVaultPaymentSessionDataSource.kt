package io.primer.android.klarna.implementation.session.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.klarna.implementation.session.data.models.CreateSessionDataResponse
import io.primer.android.klarna.implementation.session.data.models.CreateVaultPaymentSessionDataRequest

internal class RemoteKlarnaVaultPaymentSessionDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseSuspendDataSource<
        CreateSessionDataResponse,
        BaseRemoteHostRequest<CreateVaultPaymentSessionDataRequest>,
        > {
    override suspend fun execute(
        input: BaseRemoteHostRequest<CreateVaultPaymentSessionDataRequest>,
    ): CreateSessionDataResponse =
        primerHttpClient.suspendPost<
            CreateVaultPaymentSessionDataRequest,
            CreateSessionDataResponse,
            >(
            "${input.host}/klarna/payment-sessions",
            input.data,
        ).body
}

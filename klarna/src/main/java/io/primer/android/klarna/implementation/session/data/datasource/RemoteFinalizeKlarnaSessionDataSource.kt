package io.primer.android.klarna.implementation.session.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.klarna.implementation.session.data.models.FinalizeKlarnaSessionDataRequest
import io.primer.android.klarna.implementation.session.data.models.FinalizeKlarnaSessionDataResponse

internal class RemoteFinalizeKlarnaSessionDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseSuspendDataSource<
        FinalizeKlarnaSessionDataResponse,
        BaseRemoteHostRequest<FinalizeKlarnaSessionDataRequest>,
        > {
    override suspend fun execute(
        input: BaseRemoteHostRequest<FinalizeKlarnaSessionDataRequest>,
    ): FinalizeKlarnaSessionDataResponse =
        primerHttpClient.suspendPost<FinalizeKlarnaSessionDataRequest, FinalizeKlarnaSessionDataResponse>(
            "${input.host}/klarna/payment-sessions/finalize",
            input.data,
        ).body
}

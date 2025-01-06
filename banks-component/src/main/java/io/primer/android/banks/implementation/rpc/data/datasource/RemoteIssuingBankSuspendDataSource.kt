package io.primer.android.banks.implementation.rpc.data.datasource

import io.primer.android.banks.implementation.rpc.data.models.IssuingBankDataRequest
import io.primer.android.banks.implementation.rpc.data.models.IssuingBankResultDataResponse
import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient

internal class RemoteIssuingBankSuspendDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseSuspendDataSource<
        IssuingBankResultDataResponse,
        BaseRemoteHostRequest<IssuingBankDataRequest>,
        > {
    override suspend fun execute(input: BaseRemoteHostRequest<IssuingBankDataRequest>) =
        primerHttpClient.suspendPost<IssuingBankDataRequest, IssuingBankResultDataResponse>(
            url = "${input.host}/adyen/checkout",
            request = input.data,
        ).body
}

package io.primer.android.data.rpc.banks.datasource

import io.primer.android.data.base.datasource.BaseSuspendDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.rpc.banks.models.IssuingBankDataRequest
import io.primer.android.data.rpc.banks.models.IssuingBankResultDataResponse
import io.primer.android.http.PrimerHttpClient

internal class RemoteIssuingBankSuspendDataSource(
    private val primerHttpClient: PrimerHttpClient
) : BaseSuspendDataSource<
        IssuingBankResultDataResponse, BaseRemoteRequest<IssuingBankDataRequest>> {

    override suspend fun execute(input: BaseRemoteRequest<IssuingBankDataRequest>) =
        primerHttpClient.postSuspend<IssuingBankDataRequest, IssuingBankResultDataResponse>(
            "${input.configuration.coreUrl}/adyen/checkout",
            input.data
        )
}

package io.primer.android.data.rpc.banks.datasource

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.rpc.banks.models.IssuingBankDataRequest
import io.primer.android.data.rpc.banks.models.IssuingBankResultDataResponse
import io.primer.android.http.PrimerHttpClient

internal class RemoteIssuingBankFlowDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseFlowDataSource<IssuingBankResultDataResponse, BaseRemoteRequest<IssuingBankDataRequest>> {

    override fun execute(input: BaseRemoteRequest<IssuingBankDataRequest>) =
        primerHttpClient.post<IssuingBankDataRequest, IssuingBankResultDataResponse>(
            "${input.configuration.coreUrl}/adyen/checkout",
            input.data
        )
}

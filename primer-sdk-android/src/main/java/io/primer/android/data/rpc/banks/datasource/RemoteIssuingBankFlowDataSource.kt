package io.primer.android.data.rpc.banks.datasource

import io.primer.android.data.base.datasource.BaseFlowDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.rpc.banks.models.IssuingBankRequest
import io.primer.android.data.rpc.banks.models.IssuingBankResultResponse
import io.primer.android.http.PrimerHttpClient

internal class RemoteIssuingBankFlowDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseFlowDataSource<IssuingBankResultResponse, BaseRemoteRequest<IssuingBankRequest>> {

    override fun execute(input: BaseRemoteRequest<IssuingBankRequest>) =
        primerHttpClient.post<IssuingBankRequest, IssuingBankResultResponse>(
            "${input.configuration.coreUrl}/adyen/checkout",
            input.data
        )
}

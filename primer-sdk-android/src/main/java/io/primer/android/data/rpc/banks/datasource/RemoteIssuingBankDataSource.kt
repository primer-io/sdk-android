package io.primer.android.data.rpc.banks.datasource

import io.primer.android.data.base.datasource.BaseDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.rpc.banks.models.IssuingBankRequest
import io.primer.android.data.rpc.banks.models.IssuingBankResultResponse
import io.primer.android.http.PrimerHttpClient

internal class RemoteIssuingBankDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseDataSource<IssuingBankResultResponse, BaseRemoteRequest<IssuingBankRequest>> {

    override fun execute(input: BaseRemoteRequest<IssuingBankRequest>) =
        primerHttpClient.post<IssuingBankRequest, IssuingBankResultResponse>(
            "${input.configuration.coreUrl}/adyen/checkout",
            input.data
        )
}

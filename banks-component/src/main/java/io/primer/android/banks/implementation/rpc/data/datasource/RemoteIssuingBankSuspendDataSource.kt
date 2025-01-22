package io.primer.android.banks.implementation.rpc.data.datasource

import io.primer.android.banks.implementation.rpc.data.models.IssuingBankDataRequest
import io.primer.android.banks.implementation.rpc.data.models.IssuingBankResultDataResponse
import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_60S_TIMEOUT

internal class RemoteIssuingBankSuspendDataSource(
    private val primerHttpClient: PrimerHttpClient,
) : BaseSuspendDataSource<IssuingBankResultDataResponse, BaseRemoteHostRequest<IssuingBankDataRequest>> {
    override suspend fun execute(input: BaseRemoteHostRequest<IssuingBankDataRequest>) =
        primerHttpClient
            .withTimeout(PRIMER_60S_TIMEOUT)
            .suspendPost<IssuingBankDataRequest, IssuingBankResultDataResponse>(
                url = "${input.host}/adyen/checkout",
                request = input.data,
            ).body
}

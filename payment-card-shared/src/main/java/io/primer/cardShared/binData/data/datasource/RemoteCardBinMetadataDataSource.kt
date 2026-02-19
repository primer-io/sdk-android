package io.primer.cardShared.binData.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.datasource.PrimerApiVersion
import io.primer.android.core.data.datasource.toHeaderMap
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.android.core.data.network.utils.PrimerTimeouts.PRIMER_15S_TIMEOUT
import io.primer.cardShared.binData.data.model.BinDataResponse

class RemoteCardBinMetadataDataSource(
    private val httpClient: PrimerHttpClient,
    private val apiVersion: () -> PrimerApiVersion,
) : BaseSuspendDataSource<BinDataResponse, BaseRemoteHostRequest<String>> {
    override suspend fun execute(input: BaseRemoteHostRequest<String>): BinDataResponse {
        val bin = input.data
        return httpClient.withTimeout(PRIMER_15S_TIMEOUT)
            .suspendGet<BinDataResponse>(
                url = "${input.host}/v1/bin-data/$bin",
                headers = apiVersion().toHeaderMap(),
            ).body
    }
}

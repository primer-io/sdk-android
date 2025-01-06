package io.primer.cardShared.binData.data.datasource

import io.primer.android.core.data.datasource.BaseSuspendDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.network.PrimerHttpClient
import io.primer.cardShared.binData.data.model.CardBinMetadataDataNetworksResponse
import io.primer.cardShared.binData.data.model.CardNetworkDataResponse

class RemoteCardBinMetadataDataSource(private val httpClient: PrimerHttpClient) :
    BaseSuspendDataSource<List<CardNetworkDataResponse>, BaseRemoteHostRequest<String>> {
    override suspend fun execute(input: BaseRemoteHostRequest<String>): List<CardNetworkDataResponse> {
        val bin = input.data
        return httpClient.suspendGet<CardBinMetadataDataNetworksResponse>(
            "${input.host}/v1/bin-data/$bin/networks",
        ).body.networks
    }
}

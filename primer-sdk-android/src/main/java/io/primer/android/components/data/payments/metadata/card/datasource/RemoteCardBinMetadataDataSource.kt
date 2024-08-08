package io.primer.android.components.data.payments.metadata.card.datasource

import io.primer.android.components.data.payments.metadata.card.model.CardNetworkDataResponse
import io.primer.android.components.data.payments.metadata.card.model.CardBinMetadataDataNetworksResponse
import io.primer.android.data.base.datasource.BaseSuspendDataSource
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.http.PrimerHttpClient

internal class RemoteCardBinMetadataDataSource(private val httpClient: PrimerHttpClient) :
    BaseSuspendDataSource<List<CardNetworkDataResponse>, BaseRemoteRequest<String>> {

    override suspend fun execute(input: BaseRemoteRequest<String>): List<CardNetworkDataResponse> {
        val bin = input.data
        return httpClient.suspendGet<CardBinMetadataDataNetworksResponse>(
            "${input.configuration.binDataUrl}/v1/bin-data/$bin/networks"
        ).body.networks
    }
}

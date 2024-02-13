package io.primer.android.components.data.payments.metadata.card.datasource

import io.primer.android.components.data.payments.metadata.card.model.CardNetworkDataResponse
import io.primer.android.data.base.datasource.BaseCacheDataSource

internal class InMemoryCardBinMetadataDataSource :
    BaseCacheDataSource<Map<String, List<CardNetworkDataResponse>>,
        Pair<String, List<CardNetworkDataResponse>>> {

    private val cardNetworkDataResponses: HashMap<String, List<CardNetworkDataResponse>> =
        hashMapOf()

    override fun get() = cardNetworkDataResponses.toMap()

    override fun update(input: Pair<String, List<CardNetworkDataResponse>>) {
        super.update(input)
        cardNetworkDataResponses[input.first] = input.second
    }
}

package io.primer.cardShared.binData.data.datasource

import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.cardShared.binData.data.model.CardNetworkDataResponse

class InMemoryCardBinMetadataDataSource :
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

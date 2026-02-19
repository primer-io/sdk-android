package io.primer.cardShared.binData.data.datasource

import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.cardShared.binData.data.model.BinDataResponse

class InMemoryCardBinMetadataDataSource :
    BaseCacheDataSource<
        Map<String, BinDataResponse>,
        Pair<String, BinDataResponse>,
        > {
    private val binDataResponses: HashMap<String, BinDataResponse> =
        hashMapOf()

    override fun get() = binDataResponses.toMap()

    override fun update(input: Pair<String, BinDataResponse>) {
        super.update(input)
        binDataResponses[input.first] = input.second
    }
}

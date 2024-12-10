package io.primer.android.banks.implementation.rpc.data.datasource

import io.primer.android.banks.implementation.rpc.data.models.IssuingBankDataResponse
import io.primer.android.core.data.datasource.BaseCacheDataSource

internal class LocalIssuingBankDataSource :
    BaseCacheDataSource<List<IssuingBankDataResponse>, List<IssuingBankDataResponse>> {

    private val issuersList = mutableListOf<IssuingBankDataResponse>()

    override fun get() = issuersList

    override fun update(input: List<IssuingBankDataResponse>) {
        issuersList.clear()
        issuersList.addAll(input)
    }
}

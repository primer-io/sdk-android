package io.primer.android.data.rpc.banks.datasource

import io.primer.android.data.base.datasource.BaseCacheDataSource
import io.primer.android.data.rpc.banks.models.IssuingBankDataResponse

internal class LocalIssuingBankDataSource :
    BaseCacheDataSource<List<IssuingBankDataResponse>, List<IssuingBankDataResponse>> {

    private val issuersList = mutableListOf<IssuingBankDataResponse>()

    override fun get() = issuersList

    override fun update(input: List<IssuingBankDataResponse>) {
        issuersList.clear()
        issuersList.addAll(input)
    }
}

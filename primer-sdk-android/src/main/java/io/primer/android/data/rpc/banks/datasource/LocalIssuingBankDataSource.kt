package io.primer.android.data.rpc.banks.datasource

import io.primer.android.data.base.datasource.BaseFlowCacheDataSource
import io.primer.android.data.rpc.banks.models.IssuingBankDataResponse
import kotlinx.coroutines.flow.flowOf

internal class LocalIssuingBankDataSource :
    BaseFlowCacheDataSource<List<IssuingBankDataResponse>, List<IssuingBankDataResponse>> {

    private val issuersList = mutableListOf<IssuingBankDataResponse>()

    override fun get() = flowOf(issuersList)

    override fun update(input: List<IssuingBankDataResponse>) {
        issuersList.clear()
        issuersList.addAll(input)
    }
}

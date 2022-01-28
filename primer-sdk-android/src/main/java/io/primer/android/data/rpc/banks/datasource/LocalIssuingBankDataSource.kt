package io.primer.android.data.rpc.banks.datasource

import io.primer.android.data.base.datasource.BaseFlowCacheDataSource
import io.primer.android.data.rpc.banks.models.IssuingBankResponse
import kotlinx.coroutines.flow.flowOf

internal class LocalIssuingBankDataSource :
    BaseFlowCacheDataSource<List<IssuingBankResponse>, List<IssuingBankResponse>> {

    private val issuersList = mutableListOf<IssuingBankResponse>()

    override fun get() = flowOf(issuersList)

    override fun update(input: List<IssuingBankResponse>) {
        issuersList.clear()
        issuersList.addAll(input)
    }
}

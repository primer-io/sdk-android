package io.primer.android.data.rpc.retailOutlets.datasource

import io.primer.android.data.base.datasource.BaseFlowCacheDataSource
import io.primer.android.data.rpc.retailOutlets.models.RetailOutletDataResponse
import kotlinx.coroutines.flow.flowOf

internal class LocalRetailOutletDataSource :
    BaseFlowCacheDataSource<List<RetailOutletDataResponse>, List<RetailOutletDataResponse>> {

    private val retailersList = mutableListOf<RetailOutletDataResponse>()

    override fun get() = flowOf(retailersList)

    override fun update(input: List<RetailOutletDataResponse>) {
        retailersList.clear()
        retailersList.addAll(input)
    }
}

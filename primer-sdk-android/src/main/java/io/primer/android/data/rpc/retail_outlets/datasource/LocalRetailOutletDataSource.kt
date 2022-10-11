package io.primer.android.data.rpc.retail_outlets.datasource

import io.primer.android.data.base.datasource.BaseFlowCacheDataSource
import io.primer.android.data.rpc.retail_outlets.models.RetailOutletDataResponse
import kotlinx.coroutines.flow.flowOf

internal class LocalRetailOutletDataSource :
    BaseFlowCacheDataSource<List<RetailOutletDataResponse>, List<RetailOutletDataResponse>> {

    private val issuersList = mutableListOf<RetailOutletDataResponse>()

    override fun get() = flowOf(issuersList)

    override fun update(input: List<RetailOutletDataResponse>) {
        issuersList.clear()
        issuersList.addAll(input)
    }
}

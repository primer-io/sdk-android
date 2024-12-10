package io.primer.android.vouchers.retailOutlets.implementation.rpc.data.datasource

import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.vouchers.retailOutlets.implementation.rpc.data.models.RetailOutletDataResponse

internal class LocalRetailOutletDataSource :
    BaseCacheDataSource<List<RetailOutletDataResponse>, List<RetailOutletDataResponse>> {

    private val retailersList = mutableListOf<RetailOutletDataResponse>()

    override fun get() = retailersList

    override fun update(input: List<RetailOutletDataResponse>) {
        retailersList.clear()
        retailersList.addAll(input)
    }
}

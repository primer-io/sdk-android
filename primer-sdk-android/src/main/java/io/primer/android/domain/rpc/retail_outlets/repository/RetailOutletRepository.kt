package io.primer.android.domain.rpc.retail_outlets.repository

import io.primer.android.domain.rpc.retail_outlets.models.RetailOutlet
import io.primer.android.domain.rpc.retail_outlets.models.RetailOutletParams
import kotlinx.coroutines.flow.Flow

internal interface RetailOutletRepository {

    fun getRetailOutlets(params: RetailOutletParams): Flow<List<RetailOutlet>>

    fun getCachedRetailOutlets(): Flow<List<RetailOutlet>>
}

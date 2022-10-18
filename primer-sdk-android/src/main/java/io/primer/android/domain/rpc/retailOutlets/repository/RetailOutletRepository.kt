package io.primer.android.domain.rpc.retailOutlets.repository

import io.primer.android.domain.rpc.retailOutlets.models.RetailOutlet
import io.primer.android.domain.rpc.retailOutlets.models.RetailOutletParams
import kotlinx.coroutines.flow.Flow

internal interface RetailOutletRepository {

    fun getRetailOutlets(params: RetailOutletParams): Flow<List<RetailOutlet>>

    fun getCachedRetailOutlets(): Flow<List<RetailOutlet>>
}

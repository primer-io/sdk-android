package io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.repository

import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.models.RetailOutlet

internal interface RetailOutletRepository {

    suspend fun getRetailOutlets(paymentMethodConfigId: String): Result<List<RetailOutlet>>

    fun getCachedRetailOutlets(): List<RetailOutlet>
}

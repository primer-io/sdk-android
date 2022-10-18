package io.primer.android.domain.rpc.retailOutlets

import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.rpc.retailOutlets.models.RetailOutlet
import io.primer.android.domain.rpc.retailOutlets.models.RetailOutletFilterParams
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest

internal class RetailOutletFilterInteractor(
    private val retailOutletRepository: RetailOutletRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseFlowInteractor<List<RetailOutlet>, RetailOutletFilterParams>() {

    override fun execute(params: RetailOutletFilterParams) =
        retailOutletRepository.getCachedRetailOutlets()
            .mapLatest { it.filter { it.name.contains(params.text, true) } }
            .mapLatest { it.sortedBy { it.name.lowercase() } }
            .flowOn(dispatcher)
}

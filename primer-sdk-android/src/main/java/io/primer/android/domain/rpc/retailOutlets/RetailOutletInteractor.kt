package io.primer.android.domain.rpc.retailOutlets

import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.rpc.retailOutlets.models.RetailOutlet
import io.primer.android.domain.rpc.retailOutlets.models.RetailOutletParams
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.mapLatest

internal class RetailOutletInteractor(
    private val retailOutletRepository: RetailOutletRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseFlowInteractor<List<RetailOutlet>, RetailOutletParams>() {

    override fun execute(params: RetailOutletParams) =
        retailOutletRepository.getRetailOutlets(params)
            .mapLatest { it.filterNot { it.disabled } }
            .mapLatest { it.sortedBy { it.name.lowercase() } }
            .flowOn(dispatcher)
}

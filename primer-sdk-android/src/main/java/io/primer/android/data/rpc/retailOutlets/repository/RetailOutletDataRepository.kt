package io.primer.android.data.rpc.retailOutlets.repository

import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.rpc.retailOutlets.datasource.LocalRetailOutletDataSource
import io.primer.android.data.rpc.retailOutlets.datasource.RemoteRetailOutletFlowDataSource
import io.primer.android.data.rpc.retailOutlets.models.toRetailOutlet
import io.primer.android.data.rpc.retailOutlets.models.toRetailOutletRequest
import io.primer.android.domain.rpc.retailOutlets.models.RetailOutlet
import io.primer.android.domain.rpc.retailOutlets.models.RetailOutletParams
import io.primer.android.domain.rpc.retailOutlets.repository.RetailOutletRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
internal class RetailOutletDataRepository(
    private val remoteRetailOutletBankDataSource: RemoteRetailOutletFlowDataSource,
    private val localRetailOutletDataSource: LocalRetailOutletDataSource,
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : RetailOutletRepository {

    private var selectedRetailOutlet: RetailOutlet? = null

    override fun getRetailOutlets(params: RetailOutletParams) =
        localConfigurationDataSource.get().flatMapLatest { configuration ->
            remoteRetailOutletBankDataSource.execute(
                BaseRemoteRequest(
                    configuration,
                    params.toRetailOutletRequest()
                )
            )
                .onEach { localRetailOutletDataSource.update(it.result) }
                .mapLatest { data -> data.result.map { it.toRetailOutlet() } }
        }

    override fun getCachedRetailOutlets() =
        localRetailOutletDataSource.get()
            .mapLatest { result -> result.map { it.toRetailOutlet() } }

    override fun setSelectedRetailOutlet(id: String) {
        CoroutineScope(dispatcher).launch {
            selectedRetailOutlet = getCachedRetailOutlets().first().firstOrNull { it.id == id }
        }
    }

    override fun getSelectedRetailOutlet() = selectedRetailOutlet
}

package io.primer.android.data.rpc.retail_outlets.repository

import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.rpc.retail_outlets.datasource.LocalRetailOutletDataSource
import io.primer.android.data.rpc.retail_outlets.datasource.RemoteRetailOutletFlowDataSource
import io.primer.android.data.rpc.retail_outlets.models.toRetailOutlet
import io.primer.android.data.rpc.retail_outlets.models.toRetailOutletRequest
import io.primer.android.domain.rpc.retail_outlets.models.RetailOutletParams
import io.primer.android.domain.rpc.retail_outlets.repository.RetailOutletRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
internal class RetailOutletDataRepository(
    private val remoteIssuingBankDataSource: RemoteRetailOutletFlowDataSource,
    private val localRetailOutletDataSource: LocalRetailOutletDataSource,
    private val localConfigurationDataSource: LocalConfigurationDataSource
) : RetailOutletRepository {

    override fun getRetailOutlets(params: RetailOutletParams) =
        localConfigurationDataSource.get().flatMapLatest { configuration ->
            remoteIssuingBankDataSource.execute(
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
}

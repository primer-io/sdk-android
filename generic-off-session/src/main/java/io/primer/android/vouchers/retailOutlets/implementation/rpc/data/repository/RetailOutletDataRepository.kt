package io.primer.android.vouchers.retailOutlets.implementation.rpc.data.repository

import io.primer.android.configuration.data.datasource.CacheConfigurationDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.extensions.runSuspendCatching
import io.primer.android.vouchers.retailOutlets.implementation.rpc.data.datasource.LocalRetailOutletDataSource
import io.primer.android.vouchers.retailOutlets.implementation.rpc.data.datasource.RemoteRetailOutletDataSource
import io.primer.android.vouchers.retailOutlets.implementation.rpc.data.models.RetailOutletDataRequest
import io.primer.android.vouchers.retailOutlets.implementation.rpc.data.models.toRetailOutlet
import io.primer.android.vouchers.retailOutlets.implementation.rpc.domain.repository.RetailOutletRepository

internal class RetailOutletDataRepository(
    private val remoteRetailOutletBankDataSource: RemoteRetailOutletDataSource,
    private val localRetailOutletDataSource: LocalRetailOutletDataSource,
    private val configurationDataSource: CacheConfigurationDataSource
) : RetailOutletRepository {

    override suspend fun getRetailOutlets(paymentMethodConfigId: String) = runSuspendCatching {
        configurationDataSource.get().let { configuration ->
            remoteRetailOutletBankDataSource.execute(
                BaseRemoteHostRequest(
                    host = configuration.coreUrl,
                    data = RetailOutletDataRequest(paymentMethodConfigId = paymentMethodConfigId)
                )
            )
        }
    }.onSuccess { localRetailOutletDataSource.update(it.result) }
        .map { data -> data.result.map { it.toRetailOutlet() } }

    override fun getCachedRetailOutlets() =
        localRetailOutletDataSource.get().map { result -> result.toRetailOutlet() }
}

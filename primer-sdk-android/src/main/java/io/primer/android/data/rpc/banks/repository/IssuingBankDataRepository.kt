package io.primer.android.data.rpc.banks.repository

import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.rpc.banks.datasource.LocalIssuingBankDataSource
import io.primer.android.data.rpc.banks.datasource.RemoteIssuingBankFlowDataSource
import io.primer.android.data.rpc.banks.models.toIssuingBank
import io.primer.android.data.rpc.banks.models.toIssuingBankRequest
import io.primer.android.domain.rpc.banks.models.IssuingBankParams
import io.primer.android.domain.rpc.banks.repository.IssuingBankRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach

@ExperimentalCoroutinesApi
internal class IssuingBankDataRepository(
    private val remoteIssuingBankDataSource: RemoteIssuingBankFlowDataSource,
    private val localIssuingBankDataSource: LocalIssuingBankDataSource,
    private val localConfigurationDataSource: LocalConfigurationDataSource
) : IssuingBankRepository {

    override fun getIssuingBanks(params: IssuingBankParams) =
        localConfigurationDataSource.get().flatMapLatest { configuration ->
            remoteIssuingBankDataSource.execute(
                BaseRemoteRequest(
                    configuration,
                    params.toIssuingBankRequest()
                )
            )
                .onEach { localIssuingBankDataSource.update(it.result) }
                .mapLatest { data -> data.result.map { it.toIssuingBank() } }
        }

    override fun getCachedIssuingBanks() =
        localIssuingBankDataSource.get()
            .mapLatest { result -> result.map { it.toIssuingBank() } }
}

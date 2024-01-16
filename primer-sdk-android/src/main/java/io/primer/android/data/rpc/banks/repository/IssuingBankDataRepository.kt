package io.primer.android.data.rpc.banks.repository

import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.rpc.banks.datasource.LocalIssuingBankDataSource
import io.primer.android.data.rpc.banks.datasource.RemoteIssuingBankSuspendDataSource
import io.primer.android.data.rpc.banks.models.toIssuingBank
import io.primer.android.data.rpc.banks.models.toIssuingBankRequest
import io.primer.android.domain.rpc.banks.models.IssuingBankParams
import io.primer.android.domain.rpc.banks.repository.IssuingBankRepository
import io.primer.android.extensions.runSuspendCatching
import kotlinx.coroutines.ExperimentalCoroutinesApi

@ExperimentalCoroutinesApi
internal class IssuingBankDataRepository(
    private val remoteIssuingSuspendDataSource: RemoteIssuingBankSuspendDataSource,
    private val localIssuingBankDataSource: LocalIssuingBankDataSource,
    private val localConfigurationDataSource: LocalConfigurationDataSource
) : IssuingBankRepository {

    override suspend fun getIssuingBanks(params: IssuingBankParams) = runSuspendCatching {
        remoteIssuingSuspendDataSource.execute(
            BaseRemoteRequest(
                localConfigurationDataSource.getConfiguration(),
                params.toIssuingBankRequest()
            )
        )
            .also { localIssuingBankDataSource.update(it.result) }
            .result.map { it.toIssuingBank() }
    }

    override suspend fun getCachedIssuingBanks() =
        runSuspendCatching {
            localIssuingBankDataSource.get().map { it.toIssuingBank() }
        }
}

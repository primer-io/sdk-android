package io.primer.android.banks.implementation.rpc.data.repository

import io.primer.android.banks.implementation.rpc.data.datasource.LocalIssuingBankDataSource
import io.primer.android.banks.implementation.rpc.data.datasource.RemoteIssuingBankSuspendDataSource
import io.primer.android.banks.implementation.rpc.data.models.toIssuingBank
import io.primer.android.banks.implementation.rpc.data.models.toIssuingBankRequest
import io.primer.android.banks.implementation.rpc.domain.models.IssuingBankParams
import io.primer.android.banks.implementation.rpc.domain.repository.IssuingBankRepository
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.extensions.runSuspendCatching

internal class IssuingBankDataRepository(
    private val remoteIssuingSuspendDataSource: RemoteIssuingBankSuspendDataSource,
    private val localIssuingBankDataSource: LocalIssuingBankDataSource,
    private val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
) : IssuingBankRepository {
    override suspend fun getIssuingBanks(params: IssuingBankParams) =
        runSuspendCatching {
            remoteIssuingSuspendDataSource.execute(
                BaseRemoteHostRequest(
                    configurationDataSource.get().coreUrl,
                    params.toIssuingBankRequest(),
                ),
            )
                .also { localIssuingBankDataSource.update(it.result) }
                .result.map { it.toIssuingBank() }
        }

    override suspend fun getCachedIssuingBanks() =
        runSuspendCatching {
            localIssuingBankDataSource.get().map { it.toIssuingBank() }
        }
}

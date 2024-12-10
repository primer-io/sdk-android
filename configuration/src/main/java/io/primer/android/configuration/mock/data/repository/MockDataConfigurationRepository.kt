package io.primer.android.configuration.mock.data.repository

import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.configuration.mock.data.datasource.RemoteFinalizeMockedFlowDataSource
import io.primer.android.configuration.mock.domain.repository.MockConfigurationRepository
import io.primer.android.core.data.datasource.BaseCacheDataSource
import io.primer.android.core.data.model.BaseRemoteHostRequest
import io.primer.android.core.data.model.EmptyDataRequest
import io.primer.android.core.extensions.runSuspendCatching

internal class MockDataConfigurationRepository(
    private val configurationDataSource: BaseCacheDataSource<ConfigurationData, ConfigurationData>,
    private val remoteFinalizeMockedFlowDataSource: RemoteFinalizeMockedFlowDataSource
) : MockConfigurationRepository {

    override fun isMockedFlow() =
        configurationDataSource.get().clientSession.testId.isNullOrBlank().not()

    override suspend fun finalizeMockedFlow() = runSuspendCatching {
        configurationDataSource.get().let {
            remoteFinalizeMockedFlowDataSource.execute(
                BaseRemoteHostRequest(
                    it.coreUrl,
                    EmptyDataRequest()
                )
            )
        }.let { }
    }
}

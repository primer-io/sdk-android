package io.primer.android.data.mock.repository

import io.primer.android.core.data.models.EmptyDataRequest
import io.primer.android.data.base.models.BaseRemoteRequest
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.mock.datasource.RemoteFinalizeMockedFlowDataSource
import io.primer.android.domain.mock.repository.MockConfigurationRepository
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

internal class MockDataConfigurationRepository(
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val remoteFinalizeMockedFlowDataSource: RemoteFinalizeMockedFlowDataSource
) : MockConfigurationRepository {

    override fun isMockedFlow() =
        localConfigurationDataSource.getConfiguration().clientSession?.testId.isNullOrBlank().not()

    override fun finalizeMockedFlow() = localConfigurationDataSource.get().flatMapLatest {
        remoteFinalizeMockedFlowDataSource.execute(BaseRemoteRequest(it, EmptyDataRequest()))
    }.map { }
}

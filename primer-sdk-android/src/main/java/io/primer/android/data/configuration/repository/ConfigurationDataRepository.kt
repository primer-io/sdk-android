package io.primer.android.data.configuration.repository

import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.datasource.RemoteConfigurationDataSource
import io.primer.android.data.token.datasource.LocalClientTokenDataSource
import io.primer.android.domain.session.repository.ConfigurationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal class ConfigurationDataRepository(
    private val remoteConfigurationDataSource: RemoteConfigurationDataSource,
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val localClientTokenDataSource: LocalClientTokenDataSource
) : ConfigurationRepository {

    override fun fetchConfiguration(fromCache: Boolean): Flow<Unit> = when (fromCache) {
        true -> localConfigurationDataSource.getConfigurationAsFlow()
        false -> remoteConfigurationDataSource.getConfiguration(
            localClientTokenDataSource.getClientToken().configurationUrl.orEmpty()
        ).onEach { localConfigurationDataSource.updateConfiguration(it) }
    }.map { Unit }
}

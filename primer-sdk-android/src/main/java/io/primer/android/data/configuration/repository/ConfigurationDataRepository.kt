package io.primer.android.data.configuration.repository

import android.net.Uri
import io.primer.android.data.configuration.datasource.LocalConfigurationDataSource
import io.primer.android.data.configuration.datasource.RemoteConfigurationDataSource
import io.primer.android.data.configuration.datasource.RemoteConfigurationResourcesDataSource
import io.primer.android.data.token.datasource.LocalClientTokenDataSource
import io.primer.android.domain.session.models.Configuration
import io.primer.android.domain.session.repository.ConfigurationRepository
import io.primer.android.utils.buildWithQueryParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

internal class ConfigurationDataRepository(
    private val remoteConfigurationDataSource: RemoteConfigurationDataSource,
    private val remoteConfigurationResourcesDataSource: RemoteConfigurationResourcesDataSource,
    private val localConfigurationDataSource: LocalConfigurationDataSource,
    private val localClientTokenDataSource: LocalClientTokenDataSource
) : ConfigurationRepository {

    override fun fetchConfiguration(fromCache: Boolean): Flow<Configuration> = when (fromCache) {
        true -> localConfigurationDataSource.get().map { it.toConfiguration() }
        false -> remoteConfigurationDataSource.execute(
            Uri.parse(localClientTokenDataSource.get().configurationUrl.orEmpty())
                .buildWithQueryParams(mapOf(DISPLAY_METADATA_QUERY_KEY to true))
        ).flatMapLatest { configuration ->
            remoteConfigurationResourcesDataSource.execute(configuration.paymentMethods).map {
                configuration.toConfigurationData(it)
            }
        }.onEach { localConfigurationDataSource.update(it) }
            .map { it.toConfiguration() }
    }

    override fun getConfiguration(): Configuration =
        localConfigurationDataSource.getConfiguration().toConfiguration()

    private companion object {
        const val DISPLAY_METADATA_QUERY_KEY = "withDisplayMetadata"
    }
}

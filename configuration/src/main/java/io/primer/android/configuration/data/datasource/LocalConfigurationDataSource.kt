package io.primer.android.configuration.data.datasource

import io.primer.android.configuration.data.exception.MissingConfigurationException
import io.primer.android.configuration.data.model.ConfigurationData
import io.primer.android.core.data.datasource.BaseCacheDataSource

typealias CacheConfigurationDataSource = BaseCacheDataSource<ConfigurationData, ConfigurationData>

internal class LocalConfigurationDataSource :
    BaseCacheDataSource<ConfigurationData, ConfigurationData> {
    private var configuration: ConfigurationData? = null

    @Throws(MissingConfigurationException::class)
    override fun get() =
        try {
            requireNotNull(configuration)
        } catch (e: IllegalArgumentException) {
            throw MissingConfigurationException(e)
        }

    override fun update(input: ConfigurationData) {
        this.configuration = input
    }
}

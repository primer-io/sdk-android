package io.primer.android.configuration.data.datasource

import io.primer.android.configuration.data.model.ConfigurationDataResponse
import io.primer.android.core.data.datasource.BaseCacheDataSource

data class ConfigurationCache(val validUntil: Long, val clientToken: String)

typealias GlobalCacheConfigurationCacheDataSource =
    BaseCacheDataSource<
        Pair<ConfigurationCache, ConfigurationDataResponse>?,
        Pair<ConfigurationCache, ConfigurationDataResponse>,
        >

internal object GlobalConfigurationCacheDataSource :
    BaseCacheDataSource<
        Pair<ConfigurationCache, ConfigurationDataResponse>?,
        Pair<ConfigurationCache, ConfigurationDataResponse>,
        > {
    private var configurationDataCache: Pair<ConfigurationCache, ConfigurationDataResponse>? = null

    override fun get(): Pair<ConfigurationCache, ConfigurationDataResponse>? {
        return configurationDataCache
    }

    override fun update(input: Pair<ConfigurationCache, ConfigurationDataResponse>) {
        super.update(input)
        configurationDataCache = input
    }

    override fun clear() {
        super.clear()
        configurationDataCache = null
    }
}

package io.primer.android.data.configuration.datasource

import io.primer.android.data.base.datasource.BaseCacheDataSource
import io.primer.android.data.configuration.models.ConfigurationDataResponse

internal data class ConfigurationCache(val validUntil: Long, val clientToken: String)

internal object GlobalConfigurationCacheDataSource :
    BaseCacheDataSource<Pair<ConfigurationCache, ConfigurationDataResponse>?,
        Pair<ConfigurationCache, ConfigurationDataResponse>> {

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

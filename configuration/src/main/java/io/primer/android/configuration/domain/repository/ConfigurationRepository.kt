package io.primer.android.configuration.domain.repository

import io.primer.android.configuration.domain.CachePolicy
import io.primer.android.configuration.domain.model.Configuration

interface ConfigurationRepository {

    suspend fun fetchConfiguration(cachePolicy: CachePolicy): Result<Configuration>

    fun getConfiguration(): Configuration
}

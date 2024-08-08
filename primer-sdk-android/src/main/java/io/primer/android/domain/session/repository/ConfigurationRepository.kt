package io.primer.android.domain.session.repository

import io.primer.android.domain.session.CachePolicy
import io.primer.android.domain.session.models.Configuration
import kotlinx.coroutines.flow.Flow

internal interface ConfigurationRepository {

    fun fetchConfiguration(cachePolicy: CachePolicy): Flow<Configuration>

    fun getConfiguration(): Configuration
}

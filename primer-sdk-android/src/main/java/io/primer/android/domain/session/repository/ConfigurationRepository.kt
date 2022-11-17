package io.primer.android.domain.session.repository

import io.primer.android.domain.session.models.Configuration
import kotlinx.coroutines.flow.Flow

internal interface ConfigurationRepository {

    fun fetchConfiguration(fromCache: Boolean): Flow<Configuration>

    fun getConfiguration(): Configuration
}

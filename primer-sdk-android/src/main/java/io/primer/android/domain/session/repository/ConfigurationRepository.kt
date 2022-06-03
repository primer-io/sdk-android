package io.primer.android.domain.session.repository

import io.primer.android.data.configuration.models.Configuration
import kotlinx.coroutines.flow.Flow

internal interface ConfigurationRepository {

    fun fetchConfiguration(fromCache: Boolean): Flow<Configuration>
}

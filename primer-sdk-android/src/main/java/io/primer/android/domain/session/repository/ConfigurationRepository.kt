package io.primer.android.domain.session.repository

import kotlinx.coroutines.flow.Flow

internal interface ConfigurationRepository {

    fun fetchConfiguration(fromCache: Boolean): Flow<Unit>
}

package io.primer.android.domain.mock.repository

import kotlinx.coroutines.flow.Flow

internal interface MockConfigurationRepository {

    fun isMockedFlow(): Boolean

    fun finalizeMockedFlow(): Flow<Unit>
}

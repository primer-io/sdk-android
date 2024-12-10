package io.primer.android.configuration.mock.domain.repository

internal interface MockConfigurationRepository {

    fun isMockedFlow(): Boolean

    suspend fun finalizeMockedFlow(): Result<Unit>
}

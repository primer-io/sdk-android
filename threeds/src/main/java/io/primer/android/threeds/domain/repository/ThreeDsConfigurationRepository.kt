package io.primer.android.threeds.domain.repository

import io.primer.android.threeds.domain.models.ThreeDsAuthParams
import io.primer.android.threeds.domain.models.ThreeDsKeysParams

internal interface ThreeDsConfigurationRepository {

    suspend fun getConfiguration(): Result<ThreeDsKeysParams?>

    suspend fun getPreAuthConfiguration(supportedThreeDsProtocolVersions: List<String>): Result<ThreeDsAuthParams>
}

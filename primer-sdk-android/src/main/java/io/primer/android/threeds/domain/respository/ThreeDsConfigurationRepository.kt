package io.primer.android.threeds.domain.respository

import io.primer.android.threeds.domain.models.ThreeDsAuthParams
import io.primer.android.threeds.domain.models.ThreeDsKeysParams
import kotlinx.coroutines.flow.Flow

internal interface ThreeDsConfigurationRepository {

    fun getConfiguration(): Flow<ThreeDsKeysParams?>

    fun getPreAuthConfiguration(): Flow<ThreeDsAuthParams>
}

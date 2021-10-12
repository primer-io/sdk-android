package io.primer.android.threeds.domain.respository

import io.primer.android.threeds.domain.models.ThreeDsKeysParams
import io.primer.android.threeds.helpers.ProtocolVersion
import kotlinx.coroutines.flow.Flow

internal interface ThreeDsConfigurationRepository {

    fun getConfiguration(): Flow<ThreeDsKeysParams?>

    fun getProtocolVersion(): Flow<ProtocolVersion>
}

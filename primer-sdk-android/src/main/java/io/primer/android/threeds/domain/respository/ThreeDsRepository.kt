package io.primer.android.threeds.domain.respository

import io.primer.android.threeds.data.models.BeginAuthRequest
import io.primer.android.threeds.data.models.BeginAuthResponse
import io.primer.android.threeds.data.models.PostAuthResponse
import kotlinx.coroutines.flow.Flow

internal interface ThreeDsRepository {

    fun begin3DSAuth(
        token: String,
        request: BeginAuthRequest,
    ): Flow<BeginAuthResponse>

    fun continue3DSAuth(token: String): Flow<PostAuthResponse>
}

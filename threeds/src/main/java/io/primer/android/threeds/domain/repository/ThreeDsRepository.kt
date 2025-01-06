package io.primer.android.threeds.domain.repository

import io.primer.android.threeds.data.models.auth.BeginAuthResponse
import io.primer.android.threeds.data.models.postAuth.PostAuthResponse
import io.primer.android.threeds.domain.models.BaseThreeDsContinueAuthParams
import io.primer.android.threeds.domain.models.BaseThreeDsParams

internal interface ThreeDsRepository {
    suspend fun begin3DSAuth(
        token: String,
        threeDsParams: BaseThreeDsParams,
    ): Result<BeginAuthResponse>

    suspend fun continue3DSAuth(
        token: String,
        continueAuthParams: BaseThreeDsContinueAuthParams,
    ): Result<PostAuthResponse>
}

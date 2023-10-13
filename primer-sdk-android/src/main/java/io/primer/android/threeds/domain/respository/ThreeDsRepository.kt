package io.primer.android.threeds.domain.respository

import io.primer.android.threeds.data.models.auth.BeginAuthResponse
import io.primer.android.threeds.data.models.postAuth.PostAuthResponse
import io.primer.android.threeds.domain.models.BaseThreeDsContinueAuthParams
import io.primer.android.threeds.domain.models.BaseThreeDsParams
import kotlinx.coroutines.flow.Flow

internal interface ThreeDsRepository {

    fun begin3DSAuth(
        token: String,
        threeDsParams: BaseThreeDsParams
    ): Flow<BeginAuthResponse>

    fun continue3DSAuth(
        token: String,
        continueAuthParams: BaseThreeDsContinueAuthParams
    ): Flow<PostAuthResponse>
}

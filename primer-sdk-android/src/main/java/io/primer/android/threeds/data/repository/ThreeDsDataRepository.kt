package io.primer.android.threeds.data.repository

import io.primer.android.model.Model
import io.primer.android.threeds.data.models.BeginAuthRequest
import io.primer.android.threeds.data.models.BeginAuthResponse
import io.primer.android.threeds.data.models.PostAuthResponse
import io.primer.android.threeds.domain.respository.ThreeDsRepository
import kotlinx.coroutines.flow.Flow

internal class ThreeDsDataRepository(private val model: Model) : ThreeDsRepository {

    override fun begin3DSAuth(
        token: String,
        request: BeginAuthRequest,
    ): Flow<BeginAuthResponse> {
        return model.get3dsAuthToken(token, request)
    }

    override fun continue3DSAuth(token: String): Flow<PostAuthResponse> {
        return model.continue3dsAuth(
            token,
        )
    }
}

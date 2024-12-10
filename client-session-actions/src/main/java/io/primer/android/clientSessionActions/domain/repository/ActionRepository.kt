package io.primer.android.clientSessionActions.domain.repository

import io.primer.android.clientSessionActions.domain.models.BaseActionUpdateParams
import io.primer.android.configuration.domain.model.ClientSessionData

internal fun interface ActionRepository {

    suspend fun updateClientActions(params: List<BaseActionUpdateParams>): Result<ClientSessionData>
}

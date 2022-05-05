package io.primer.android.domain.action.repository

import io.primer.android.domain.ClientSessionData
import io.primer.android.domain.action.models.BaseActionUpdateParams
import kotlinx.coroutines.flow.Flow

internal interface ActionRepository {

    fun updateClientActions(params: BaseActionUpdateParams): Flow<ClientSessionData>
}

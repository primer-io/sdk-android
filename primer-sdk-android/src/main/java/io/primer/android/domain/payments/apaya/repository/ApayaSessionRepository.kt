package io.primer.android.domain.payments.apaya.repository

import io.primer.android.domain.payments.apaya.models.ApayaSession
import io.primer.android.domain.payments.apaya.models.ApayaSessionParams
import kotlinx.coroutines.flow.Flow

internal interface ApayaSessionRepository {

    fun createClientSession(params: ApayaSessionParams): Flow<ApayaSession>
}

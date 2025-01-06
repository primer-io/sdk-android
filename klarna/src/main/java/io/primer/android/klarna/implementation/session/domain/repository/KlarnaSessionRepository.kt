package io.primer.android.klarna.implementation.session.domain.repository

import io.primer.android.PrimerSessionIntent
import io.primer.android.klarna.implementation.session.domain.models.KlarnaSession

internal interface KlarnaSessionRepository {
    suspend fun createSession(
        surcharge: Int?,
        primerSessionIntent: PrimerSessionIntent,
    ): Result<KlarnaSession>
}

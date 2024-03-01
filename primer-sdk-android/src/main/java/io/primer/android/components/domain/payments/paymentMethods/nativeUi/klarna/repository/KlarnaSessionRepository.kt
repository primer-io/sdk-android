package io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository

import io.primer.android.PrimerSessionIntent
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaSession

internal interface KlarnaSessionRepository {

    suspend fun createSession(
        surcharge: Int?,
        primerSessionIntent: PrimerSessionIntent
    ): Result<KlarnaSession>
}

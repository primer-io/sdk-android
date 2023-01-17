package io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaSession
import kotlinx.coroutines.flow.Flow

internal interface KlarnaSessionRepository {

    fun createSession(): Flow<KlarnaSession>
}

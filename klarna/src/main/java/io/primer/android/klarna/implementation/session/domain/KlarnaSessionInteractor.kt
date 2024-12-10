package io.primer.android.klarna.implementation.session.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.klarna.implementation.session.domain.models.KlarnaSession
import io.primer.android.klarna.implementation.session.domain.models.KlarnaSessionParams
import io.primer.android.klarna.implementation.session.domain.repository.KlarnaSessionRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

internal class KlarnaSessionInteractor(
    private val klarnaSessionRepository: KlarnaSessionRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<KlarnaSession, KlarnaSessionParams>() {
    override suspend fun performAction(params: KlarnaSessionParams): Result<KlarnaSession> =
        klarnaSessionRepository.createSession(params.surcharge, params.primerSessionIntent)
}

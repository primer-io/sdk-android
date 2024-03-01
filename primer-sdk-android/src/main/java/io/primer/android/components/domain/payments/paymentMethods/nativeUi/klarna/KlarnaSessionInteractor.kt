package io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaSession
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaSessionParams
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository.KlarnaSessionRepository
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.domain.error.ErrorMapperType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class KlarnaSessionInteractor(
    private val klarnaSessionRepository: KlarnaSessionRepository,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<KlarnaSession, KlarnaSessionParams>() {
    override suspend fun performAction(params: KlarnaSessionParams): Result<KlarnaSession> =
        withContext(dispatcher) {
            klarnaSessionRepository.createSession(params.surcharge, params.primerSessionIntent)
                .onFailure { baseErrorEventResolver.resolve(it, ErrorMapperType.SESSION_CREATE) }
        }
}

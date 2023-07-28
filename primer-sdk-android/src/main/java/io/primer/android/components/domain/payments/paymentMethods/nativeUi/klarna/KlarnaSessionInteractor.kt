package io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaSession
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository.KlarnaSessionRepository
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.base.None
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.extensions.doOnError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

internal class KlarnaSessionInteractor(
    private val klarnaSessionRepository: KlarnaSessionRepository,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseFlowInteractor<KlarnaSession, None>() {
    override fun execute(params: None): Flow<KlarnaSession> {
        return klarnaSessionRepository.createSession()
            .doOnError { baseErrorEventResolver.resolve(it, ErrorMapperType.SESSION_CREATE) }
            .flowOn(dispatcher)
    }
}

package io.primer.android.klarna.implementation.session.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.klarna.implementation.session.domain.models.KlarnaCustomerTokenParam
import io.primer.android.klarna.implementation.session.domain.repository.FinalizeKlarnaSessionRepository
import io.primer.android.klarna.implementation.session.data.models.FinalizeKlarnaSessionDataResponse
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class FinalizeKlarnaSessionInteractor(
    private val finalizeKlarnaSessionRepository: FinalizeKlarnaSessionRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<FinalizeKlarnaSessionDataResponse, KlarnaCustomerTokenParam>() {
    override suspend fun performAction(
        params: KlarnaCustomerTokenParam
    ): Result<FinalizeKlarnaSessionDataResponse> = withContext(dispatcher) {
        finalizeKlarnaSessionRepository.finalize(params)
    }
}

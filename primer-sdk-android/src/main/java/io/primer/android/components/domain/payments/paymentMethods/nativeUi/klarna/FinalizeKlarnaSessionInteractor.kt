package io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.FinalizeKlarnaSessionDataResponse
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaCustomerTokenParam
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository.FinalizeKlarnaSessionRepository
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.domain.error.ErrorMapperType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class FinalizeKlarnaSessionInteractor(
    private val finalizeKlarnaSessionRepository: FinalizeKlarnaSessionRepository,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<FinalizeKlarnaSessionDataResponse, KlarnaCustomerTokenParam>() {
    override suspend fun performAction(
        params: KlarnaCustomerTokenParam
    ): Result<FinalizeKlarnaSessionDataResponse> = withContext(dispatcher) {
        finalizeKlarnaSessionRepository.finalize(params)
            .onFailure { baseErrorEventResolver.resolve(it, ErrorMapperType.SESSION_CREATE) }
    }
}

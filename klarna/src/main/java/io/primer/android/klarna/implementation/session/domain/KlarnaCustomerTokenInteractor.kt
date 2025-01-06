package io.primer.android.klarna.implementation.session.domain

import io.primer.android.core.domain.BaseSuspendInteractor
import io.primer.android.klarna.implementation.session.data.models.CreateCustomerTokenDataResponse
import io.primer.android.klarna.implementation.session.domain.models.KlarnaCustomerTokenParam
import io.primer.android.klarna.implementation.session.domain.repository.KlarnaCustomerTokenRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class KlarnaCustomerTokenInteractor(
    private val klarnaCustomerTokenRepository: KlarnaCustomerTokenRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseSuspendInteractor<CreateCustomerTokenDataResponse, KlarnaCustomerTokenParam>() {
    override suspend fun performAction(params: KlarnaCustomerTokenParam): Result<CreateCustomerTokenDataResponse> =
        withContext(dispatcher) {
            klarnaCustomerTokenRepository.createCustomerToken(params)
        }
}

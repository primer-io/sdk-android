package io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateCustomerTokenDataResponse
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaCustomerTokenParam
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository.KlarnaCustomerTokenRepository
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseSuspendInteractor
import io.primer.android.domain.error.ErrorMapperType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class KlarnaCustomerTokenInteractor(
    private val klarnaCustomerTokenRepository: KlarnaCustomerTokenRepository,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseSuspendInteractor<CreateCustomerTokenDataResponse, KlarnaCustomerTokenParam>() {
    override suspend fun performAction(
        params: KlarnaCustomerTokenParam
    ): Result<CreateCustomerTokenDataResponse> = withContext(dispatcher) {
        klarnaCustomerTokenRepository.createCustomerToken(params)
            .onFailure { baseErrorEventResolver.resolve(it, ErrorMapperType.SESSION_CREATE) }
    }
}

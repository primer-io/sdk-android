package io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna

import io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models.CreateCustomerTokenDataResponse
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaCustomerTokenParam
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.repository.KlarnaCustomerTokenRepository
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.extensions.doOnError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

internal class KlarnaCustomerTokenInteractor(
    private val klarnaCustomerTokenRepository: KlarnaCustomerTokenRepository,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseFlowInteractor<CreateCustomerTokenDataResponse, KlarnaCustomerTokenParam>() {
    override fun execute(params: KlarnaCustomerTokenParam): Flow<CreateCustomerTokenDataResponse> {
        return klarnaCustomerTokenRepository.createCustomerToken(params)
            .doOnError { baseErrorEventResolver.resolve(it, ErrorMapperType.SESSION_CREATE) }
            .flowOn(dispatcher)
    }
}

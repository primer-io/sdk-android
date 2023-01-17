package io.primer.android.components.domain.payments.paymentMethods.nativeUi.async.redirect

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.async.redirect.models.AsyncPaymentMethodConfig
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.async.redirect.models.AsyncPaymentMethodParams
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.async.redirect.repository.AsyncPaymentMethodRepository
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.extensions.doOnError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

internal class AsyncPaymentMethodConfigInteractor(
    private val asyncPaymentMethodRepository: AsyncPaymentMethodRepository,
    private val eventResolver: BaseErrorEventResolver,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseFlowInteractor<AsyncPaymentMethodConfig, AsyncPaymentMethodParams>() {
    override fun execute(params: AsyncPaymentMethodParams): Flow<AsyncPaymentMethodConfig> {
        return asyncPaymentMethodRepository.getPaymentMethodConfig(params.paymentMethodType)
            .doOnError { eventResolver.resolve(it, ErrorMapperType.DEFAULT) }
            .flowOn(dispatcher)
    }
}

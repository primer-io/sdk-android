package io.primer.android.payments.core.status.domain

import io.primer.android.core.domain.BaseFlowInteractor
import io.primer.android.core.extensions.onError
import io.primer.android.errors.data.exception.PaymentMethodCancelledException
import io.primer.android.payments.core.status.domain.model.AsyncStatus
import io.primer.android.payments.core.status.domain.model.AsyncStatusParams
import io.primer.android.payments.core.status.domain.repository.AsyncPaymentMethodStatusRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlin.coroutines.cancellation.CancellationException

typealias AsyncPaymentMethodPollingInteractor = BaseFlowInteractor<AsyncStatus, AsyncStatusParams>

internal class DefaultAsyncPaymentMethodPollingInteractor(
    private val paymentMethodStatusRepository: AsyncPaymentMethodStatusRepository,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseFlowInteractor<AsyncStatus, AsyncStatusParams>() {
    override fun execute(params: AsyncStatusParams) =
        paymentMethodStatusRepository.getAsyncStatus(
            params.url,
        ).flowOn(dispatcher)
            .onError { throwable ->
                when (throwable) {
                    is CancellationException -> throw PaymentMethodCancelledException(
                        params.paymentMethodType,
                    )

                    else -> throw throwable
                }
            }
}

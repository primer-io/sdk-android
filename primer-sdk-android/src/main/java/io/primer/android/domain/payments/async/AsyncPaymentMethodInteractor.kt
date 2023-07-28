package io.primer.android.domain.payments.async

import io.primer.android.data.payments.status.exception.AsyncFlowIncompleteException
import io.primer.android.data.payments.exception.PaymentMethodCancelledException
import io.primer.android.domain.base.BaseErrorEventResolver
import io.primer.android.domain.base.BaseFlowInteractor
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.async.models.AsyncMethodParams
import io.primer.android.domain.payments.async.models.AsyncStatus
import io.primer.android.domain.payments.async.repository.AsyncPaymentMethodStatusRepository
import io.primer.android.domain.payments.helpers.ResumeEventResolver
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.extensions.doOnError
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry

internal class AsyncPaymentMethodInteractor(
    private val paymentMethodStatusRepository: AsyncPaymentMethodStatusRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val resumeEventResolver: ResumeEventResolver,
    private val baseErrorEventResolver: BaseErrorEventResolver,
    private val eventDispatcher: EventDispatcher,
    override val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : BaseFlowInteractor<AsyncStatus, AsyncMethodParams>() {

    override fun execute(params: AsyncMethodParams) = paymentMethodStatusRepository.getAsyncStatus(
        params.url,
    ).retry {
        (it is AsyncFlowIncompleteException).also { retrying ->
            if (retrying) delay(POLL_DELAY)
        }
    }.flowOn(dispatcher)
        .onEach {
            resumeEventResolver.resolve(
                paymentMethodRepository.getPaymentMethod().paymentInstrumentType,
                paymentMethodRepository.getPaymentMethod().isVaulted,
                it.resumeToken
            )
        }.doOnError {
            eventDispatcher.dispatchEvent(CheckoutEvent.AsyncFlowPollingError)
            when (it) {
                is CancellationException -> baseErrorEventResolver.resolve(
                    PaymentMethodCancelledException(
                        params.paymentMethodType
                    ),
                    ErrorMapperType.DEFAULT
                )
                else -> baseErrorEventResolver.resolve(
                    it,
                    ErrorMapperType.DEFAULT
                )
            }
        }

    private companion object {

        const val POLL_DELAY = 1000L
    }
}

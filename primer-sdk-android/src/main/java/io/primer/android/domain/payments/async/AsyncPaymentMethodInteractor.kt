package io.primer.android.domain.payments.async

import io.primer.android.completion.ResumeHandler
import io.primer.android.data.exception.AsyncFlowIncompleteException
import io.primer.android.domain.payments.async.repository.AsyncPaymentMethodStatusRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.EventDispatcher
import io.primer.android.extensions.doOnError
import io.primer.android.extensions.toResumeErrorEvent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.retry

internal class AsyncPaymentMethodInteractor(
    private val paymentMethodStatusRepository: AsyncPaymentMethodStatusRepository,
    private val eventDispatcher: EventDispatcher,
    private val resumeHandler: ResumeHandler,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    fun getPaymentFlowStatus(url: String) = paymentMethodStatusRepository.getAsyncStatus(url)
        .retry {
            (it is AsyncFlowIncompleteException).also { retrying ->
                if (retrying) delay(POLL_DELAY)
            }
        }.flowOn(dispatcher)
        .onEach {
            eventDispatcher.dispatchEvent(
                CheckoutEvent.ResumeSuccess(
                    it.resumeToken,
                    resumeHandler
                )
            )
        }.doOnError {
            eventDispatcher.dispatchEvent(
                it.toResumeErrorEvent(it.message)
            )
        }

    private companion object {

        const val POLL_DELAY = 1000L
    }
}

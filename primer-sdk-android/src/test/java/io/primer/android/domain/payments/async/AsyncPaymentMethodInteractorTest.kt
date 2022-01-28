package io.primer.android.domain.payments.async

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.completion.ResumeHandlerFactory
import io.primer.android.domain.payments.async.models.AsyncMethodParams
import io.primer.android.domain.payments.async.models.AsyncStatus
import io.primer.android.domain.payments.async.repository.AsyncPaymentMethodStatusRepository
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.CheckoutEventType
import io.primer.android.events.EventDispatcher
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.UUID

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
class AsyncPaymentMethodInteractorTest {

    @RelaxedMockK
    internal lateinit var asyncPaymentMethodStatusRepository: AsyncPaymentMethodStatusRepository

    @RelaxedMockK
    internal lateinit var paymentMethodRepository: PaymentMethodRepository

    @RelaxedMockK
    internal lateinit var eventDispatcher: EventDispatcher

    @RelaxedMockK
    internal lateinit var resumeHandlerFactory: ResumeHandlerFactory

    private val testCoroutineDispatcher = TestCoroutineDispatcher()

    private lateinit var interactor: AsyncPaymentMethodInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor =
            AsyncPaymentMethodInteractor(
                asyncPaymentMethodStatusRepository,
                paymentMethodRepository,
                eventDispatcher,
                resumeHandlerFactory,
                testCoroutineDispatcher
            )
    }

    @Test
    fun `getPaymentFlowStatus() should dispatch resume success event when getAsyncStatus was success`() {
        every { asyncPaymentMethodStatusRepository.getAsyncStatus(any()) }.returns(
            flowOf(
                AsyncStatus(UUID.randomUUID().toString())
            )
        )
        testCoroutineDispatcher.runBlockingTest {
            interactor(AsyncMethodParams("")).first()
        }

        val event = slot<CheckoutEvent>()

        verify { asyncPaymentMethodStatusRepository.getAsyncStatus(any()) }
        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assert(event.captured.type == CheckoutEventType.RESUME_SUCCESS)
    }

    @Test
    fun `getPaymentFlowStatus() should dispatch resume error event when getAsyncStatus failed`() {
        every { asyncPaymentMethodStatusRepository.getAsyncStatus(any()) }.returns(
            flow { throw Exception("Validation failed.") }
        )
        assertThrows<Exception> {
            testCoroutineDispatcher.runBlockingTest {
                interactor(AsyncMethodParams("")).first()
            }
        }

        val event = slot<CheckoutEvent>()

        verify { asyncPaymentMethodStatusRepository.getAsyncStatus(any()) }
        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assert(event.captured.type == CheckoutEventType.RESUME_ERR0R)
    }
}

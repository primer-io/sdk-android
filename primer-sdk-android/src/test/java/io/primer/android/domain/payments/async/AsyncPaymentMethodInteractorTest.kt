package io.primer.android.domain.payments.async

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.exception.PaymentMethodCancelledException
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.domain.error.CheckoutErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.async.models.AsyncMethodParams
import io.primer.android.domain.payments.async.models.AsyncStatus
import io.primer.android.domain.payments.async.repository.AsyncPaymentMethodStatusRepository
import io.primer.android.domain.payments.helpers.ResumeEventResolver
import io.primer.android.events.EventDispatcher
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
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
    internal lateinit var resumeEventResolver: ResumeEventResolver

    @RelaxedMockK
    internal lateinit var errorEventResolver: CheckoutErrorEventResolver

    @RelaxedMockK
    internal lateinit var eventDispather: EventDispatcher

    private lateinit var interactor: AsyncPaymentMethodInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor =
            AsyncPaymentMethodInteractor(
                asyncPaymentMethodStatusRepository,
                paymentMethodRepository,
                resumeEventResolver,
                errorEventResolver,
                eventDispather
            )
    }

    @Test
    fun `getPaymentFlowStatus() should dispatch resume success event when getAsyncStatus was success`() {
        val asyncStatus = mockk<AsyncStatus>(relaxed = true)
        val paymentMethodTokenInternal = mockk<PaymentMethodTokenInternal>(relaxed = true)

        every { asyncStatus.resumeToken }.returns(
            UUID.randomUUID().toString()
        )

        every { paymentMethodTokenInternal.paymentInstrumentType }.returns(
            PaymentMethodType.HOOLAH.name
        )
        every { paymentMethodRepository.getPaymentMethod() }.returns(
            paymentMethodTokenInternal
        )
        coEvery { asyncPaymentMethodStatusRepository.getAsyncStatus(any()) }.returns(
            flowOf(
                asyncStatus
            )
        )
        runTest {
            interactor(
                AsyncMethodParams(
                    "",
                    paymentMethodTokenInternal.paymentInstrumentType
                )
            ).first()
        }

        val paymentMethodType = slot<String>()
        val resumeToken = slot<String>()

        coVerify { asyncPaymentMethodStatusRepository.getAsyncStatus(any()) }
        verify { resumeEventResolver.resolve(capture(paymentMethodType), capture(resumeToken)) }

        assert(paymentMethodType.captured == PaymentMethodType.HOOLAH.name)
        assert(resumeToken.captured == asyncStatus.resumeToken)
    }

    @Test
    fun `getPaymentFlowStatus() should dispatch resume error event when getAsyncStatus failed`() {
        val exception = mockk<Exception>(relaxed = true)
        every { exception.message }.returns("Failed to get status.")
        coEvery { asyncPaymentMethodStatusRepository.getAsyncStatus(any()) }.returns(
            flow { throw exception }
        )
        assertThrows<Exception> {
            runTest {
                interactor(AsyncMethodParams("", PaymentMethodType.HOOLAH.name)).first()
            }
        }

        val event = slot<Throwable>()

        coVerify { asyncPaymentMethodStatusRepository.getAsyncStatus(any()) }
        verify { errorEventResolver.resolve(capture(event), ErrorMapperType.DEFAULT) }

        assert(event.captured.javaClass == Exception::class.java)
    }

    @Test
    fun `getPaymentFlowStatus() should dispatch error event with PaymentMethodCancelledException when getAsyncStatus failed with CancellationException`() {
        val exception = mockk<CancellationException>(relaxed = true)
        every { exception.message }.returns("Job cancelled.")
        coEvery { asyncPaymentMethodStatusRepository.getAsyncStatus(any()) }.returns(
            flow { throw exception }
        )
        assertThrows<CancellationException> {
            runTest {
                interactor(AsyncMethodParams("", PaymentMethodType.HOOLAH.name)).first()
            }
        }

        val event = slot<Throwable>()

        coVerify { asyncPaymentMethodStatusRepository.getAsyncStatus(any()) }
        verify { errorEventResolver.resolve(capture(event), ErrorMapperType.DEFAULT) }

        assert(event.captured.javaClass == PaymentMethodCancelledException::class.java)
    }
}

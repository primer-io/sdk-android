package io.primer.android.domain.payments.create

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.error.CheckoutErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.create.model.CreatePaymentParams
import io.primer.android.domain.payments.create.model.PaymentResult
import io.primer.android.domain.payments.create.repository.CreatePaymentRepository
import io.primer.android.domain.payments.helpers.PaymentResultEventsResolver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.lang.Exception

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class CreatePaymentInteractorTest {

    @RelaxedMockK
    internal lateinit var createPaymentRepository: CreatePaymentRepository

    @RelaxedMockK
    internal lateinit var paymentResultEventsResolver: PaymentResultEventsResolver

    @RelaxedMockK
    internal lateinit var errorEventResolver: CheckoutErrorEventResolver

    @RelaxedMockK
    internal lateinit var logReporter: LogReporter

    private lateinit var interactor: CreatePaymentInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor =
            CreatePaymentInteractor(
                createPaymentRepository,
                paymentResultEventsResolver,
                logReporter,
                errorEventResolver
            )
    }

    @Test
    fun `execute() should return payment id when CreatePaymentsRepository createPayment() was success`() {
        val createPaymentParams = mockk<CreatePaymentParams>(relaxed = true)
        val paymentResult = mockk<PaymentResult>(relaxed = true)

        every { createPaymentRepository.createPayment(any()) }.returns(flowOf(paymentResult))

        runTest {
            val result = interactor(createPaymentParams).first()
            Assertions.assertEquals(paymentResult.payment.id, result)
        }

        verify { createPaymentRepository.createPayment(any()) }
    }

    @Test
    fun `execute() should dispatch payment result events when CreatePaymentsRepository createPayment() was success`() {
        val createPaymentParams = mockk<CreatePaymentParams>(relaxed = true)
        val paymentResult = mockk<PaymentResult>(relaxed = true)

        every { createPaymentRepository.createPayment(any()) }.returns(flowOf(paymentResult))
        runTest {
            interactor(createPaymentParams).first()
        }

        verify { createPaymentRepository.createPayment(any()) }
        verify {
            paymentResultEventsResolver.resolve(
                paymentResult,
                createPaymentParams.resumeHandler
            )
        }
    }

    @Test
    fun `execute() should dispatch payment error events when CreatePaymentsRepository createPayment() failed`() {
        val createPaymentParams = mockk<CreatePaymentParams>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)

        every { createPaymentRepository.createPayment(any()) }.returns(flow { throw exception })

        assertThrows<Exception> {
            runTest {
                interactor(createPaymentParams).first()
            }
        }

        verify { createPaymentRepository.createPayment(any()) }
        verify {
            errorEventResolver.resolve(
                exception,
                ErrorMapperType.PAYMENT_CREATE
            )
        }
    }
}

package io.primer.android.payments.core.create.domain

import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.payments.InstantExecutorExtension
import io.primer.android.payments.core.create.domain.model.CreatePaymentParams
import io.primer.android.payments.core.create.domain.model.PaymentResult
import io.primer.android.payments.core.create.domain.repository.CreatePaymentRepository
import io.primer.android.payments.core.helpers.PaymentDecisionResolver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertTrue

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class CreatePaymentInteractorTest {
    @RelaxedMockK
    internal lateinit var createPaymentRepository: CreatePaymentRepository

    @RelaxedMockK
    internal lateinit var paymentDecisionResolver: PaymentDecisionResolver

    @RelaxedMockK
    internal lateinit var logReporter: LogReporter

    private lateinit var interactor: CreatePaymentInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor =
            DefaultCreatePaymentInteractor(
                createPaymentRepository,
                paymentDecisionResolver,
                logReporter,
            )
    }

    @Test
    fun `execute() should return payment id when CreatePaymentsRepository createPayment() was success`() {
        val createPaymentParams = mockk<CreatePaymentParams>(relaxed = true)
        val paymentResult = mockk<PaymentResult>(relaxed = true)

        coEvery { createPaymentRepository.createPayment(any()) }.returns(Result.success(paymentResult))

        runTest {
            val result = interactor(createPaymentParams).getOrNull()
            Assertions.assertEquals(paymentResult.payment.id, result?.payment?.id)
        }

        coVerify { createPaymentRepository.createPayment(any()) }
    }

    @Test
    fun `execute() should dispatch payment result events when CreatePaymentsRepository createPayment() was success`() {
        val createPaymentParams = mockk<CreatePaymentParams>(relaxed = true)
        val paymentResult = mockk<PaymentResult>(relaxed = true)

        coEvery { createPaymentRepository.createPayment(any()) }.returns(Result.success(paymentResult))
        runTest {
            interactor(createPaymentParams).getOrNull()
        }

        coVerify { createPaymentRepository.createPayment(any()) }
        verify { paymentDecisionResolver.resolve(paymentResult) }
    }

    @Test
    fun `execute() should dispatch payment error events when CreatePaymentsRepository createPayment() failed`() {
        val createPaymentParams = mockk<CreatePaymentParams>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)

        coEvery { createPaymentRepository.createPayment(any()) }.returns(Result.failure(exception))

        runTest {
            val result = interactor(createPaymentParams).exceptionOrNull()
            assertTrue(result is Exception)
        }

        coVerify { createPaymentRepository.createPayment(any()) }
    }
}

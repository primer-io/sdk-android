package io.primer.android.payments.core.resume.domain

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.core.create.domain.model.PaymentResult
import io.primer.android.payments.core.helpers.PaymentDecisionResolver
import io.primer.android.payments.core.resume.domain.models.ResumeParams
import io.primer.android.payments.core.resume.domain.respository.ResumePaymentsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExperimentalCoroutinesApi
@ExtendWith(MockKExtension::class)
class DefaultResumePaymentInteractorTest {
    private lateinit var resumePaymentsRepository: ResumePaymentsRepository
    private lateinit var paymentDecisionResolver: PaymentDecisionResolver
    private lateinit var logReporter: LogReporter
    private lateinit var interactor: DefaultResumePaymentInteractor

    @BeforeEach
    fun setup() {
        resumePaymentsRepository = mockk()
        paymentDecisionResolver = mockk()
        logReporter = mockk(relaxed = true)
        interactor =
            DefaultResumePaymentInteractor(
                resumePaymentsRepository = resumePaymentsRepository,
                paymentDecisionResolver = paymentDecisionResolver,
                logReporter = logReporter,
            )
    }

    @Test
    fun `performAction calls resumePayment and resolve when payment result is success`() =
        runTest {
            // Given
            val resumeParams = ResumeParams(paymentId = "paymentId", resumeToken = "resumeToken")
            val payment = mockk<Payment>()
            val paymentResult = mockk<PaymentResult>()
            val paymentDecision = PaymentDecision.Success(payment)

            coEvery { resumePaymentsRepository.resumePayment(any(), any()) } returns Result.success(paymentResult)
            coEvery { paymentDecisionResolver.resolve(any()) } returns paymentDecision

            // When
            val result = interactor(resumeParams)

            // Then
            assertTrue(result.isSuccess)
            assertEquals(paymentDecision, result.getOrNull())
            coVerify { resumePaymentsRepository.resumePayment(resumeParams.paymentId, resumeParams.resumeToken) }
            coVerify { paymentDecisionResolver.resolve(paymentResult) }
            verify { logReporter.debug("Resuming payment with id: ${resumeParams.paymentId}") }
        }

    @Test
    fun `performAction handles result failure`() =
        runTest {
            // Given
            val resumeParams = ResumeParams(paymentId = "paymentId", resumeToken = "resumeToken")
            val exception = RuntimeException("Some error")

            coEvery { resumePaymentsRepository.resumePayment(any(), any()) } returns Result.failure(exception)

            // When
            val result = interactor(resumeParams)

            // Then
            assertTrue(result.isFailure)
            assertEquals(exception, result.exceptionOrNull())
            coVerify { resumePaymentsRepository.resumePayment(resumeParams.paymentId, resumeParams.resumeToken) }
            verify { logReporter.debug("Resuming payment with id: ${resumeParams.paymentId}") }
        }
}

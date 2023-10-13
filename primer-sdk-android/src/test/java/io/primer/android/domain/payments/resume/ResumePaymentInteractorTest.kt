package io.primer.android.domain.payments.resume

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.domain.error.CheckoutErrorEventResolver
import io.primer.android.domain.error.ErrorMapperType
import io.primer.android.domain.payments.create.model.PaymentResult
import io.primer.android.domain.payments.helpers.PaymentResultEventsResolver
import io.primer.android.domain.payments.resume.models.ResumeParams
import io.primer.android.domain.payments.resume.respository.ResumePaymentsRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.lang.Exception

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class ResumePaymentInteractorTest {

    @RelaxedMockK
    internal lateinit var resumePaymentsRepository: ResumePaymentsRepository

    @RelaxedMockK
    internal lateinit var paymentResultEventsResolver: PaymentResultEventsResolver

    @RelaxedMockK
    internal lateinit var errorEventResolver: CheckoutErrorEventResolver

    private lateinit var interactor: ResumePaymentInteractor

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        interactor =
            ResumePaymentInteractor(
                resumePaymentsRepository,
                paymentResultEventsResolver,
                errorEventResolver
            )
    }

    @Test
    fun `execute() should dispatch payment result events when ResumePaymentsRepository resumePayment() was success`() {
        val resumePaymentParams = mockk<ResumeParams>(relaxed = true)
        val paymentResult = mockk<PaymentResult>(relaxed = true)

        every {
            resumePaymentsRepository.resumePayment(
                any(),
                any()
            )
        }.returns(flowOf(paymentResult))

        runTest {
            interactor(resumePaymentParams).first()
        }

        verify { resumePaymentsRepository.resumePayment(any(), any()) }
        verify {
            paymentResultEventsResolver.resolve(
                paymentResult,
                resumePaymentParams.resumeHandler
            )
        }
    }

    @Test
    fun `execute() should dispatch payment error events when ResumePaymentsRepository resumePayment() failed`() {
        val resumePaymentParams = mockk<ResumeParams>(relaxed = true)
        val exception = mockk<Exception>(relaxed = true)

        every {
            resumePaymentsRepository.resumePayment(
                any(),
                any()
            )
        }.returns(flow { throw exception })

        assertThrows<Exception> {
            runTest {
                interactor(resumePaymentParams).first()
            }
        }

        verify { resumePaymentsRepository.resumePayment(any(), any()) }
        verify {
            errorEventResolver.resolve(
                exception,
                ErrorMapperType.PAYMENT_RESUME
            )
        }
    }
}

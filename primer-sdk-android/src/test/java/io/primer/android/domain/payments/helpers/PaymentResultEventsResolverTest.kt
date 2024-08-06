package io.primer.android.domain.payments.helpers

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.completion.PrimerResumeDecisionHandler
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.data.configuration.models.PaymentMethodType
import io.primer.android.data.payments.create.models.PaymentStatus
import io.primer.android.data.tokenization.models.PaymentMethodTokenInternal
import io.primer.android.domain.error.models.PaymentError
import io.primer.android.domain.payments.create.model.PaymentResult
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.CheckoutEventType
import io.primer.android.events.EventDispatcher
import io.primer.android.threeds.domain.respository.PaymentMethodRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class PaymentResultEventsResolverTest {

    @RelaxedMockK
    internal lateinit var paymentMethodRepository: PaymentMethodRepository

    @RelaxedMockK
    internal lateinit var eventDispatcher: EventDispatcher

    @RelaxedMockK
    internal lateinit var logReporter: LogReporter

    private lateinit var paymentResultEventsResolver: PaymentResultEventsResolver

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        paymentResultEventsResolver =
            PaymentResultEventsResolver(paymentMethodRepository, eventDispatcher, logReporter)
    }

    @Test
    fun `resolve() should call continueWithNewClientToken when payment status is PENDING`() {
        val paymentResult = mockk<PaymentResult>(relaxed = true)
        val resumeHandler = mockk<PrimerResumeDecisionHandler>(relaxed = true)

        every { paymentResult.paymentStatus }.returns(PaymentStatus.PENDING)

        runTest {
            paymentResultEventsResolver.resolve(paymentResult, resumeHandler)
        }

        val clientToken = slot<String>()

        verify {
            resumeHandler.continueWithNewClientToken(capture(clientToken))
        }

        assertEquals(paymentResult.clientToken, clientToken.captured)
    }

    @Test
    fun `resolve() should call continueWithNewClientToken when payment status is PENDING and clientToken is empty`() {
        val paymentResult = mockk<PaymentResult>(relaxed = true)
        val resumeHandler = mockk<PrimerResumeDecisionHandler>(relaxed = true)

        every { paymentResult.paymentStatus }.returns(PaymentStatus.PENDING)
        every { paymentResult.clientToken }.returns(null)

        runTest {
            paymentResultEventsResolver.resolve(paymentResult, resumeHandler)
        }

        val clientToken = slot<String>()

        verify {
            resumeHandler.continueWithNewClientToken(capture(clientToken))
        }

        assertEquals("", clientToken.captured)
    }

    @Test
    fun `resolve() should dispatch CHECKOUT_AUTO_ERROR type when payment status is FAILED`() {
        val token = mockk<PaymentMethodTokenInternal>(relaxed = true)
        val paymentResult = mockk<PaymentResult>(relaxed = true)
        val resumeHandler = mockk<PrimerResumeDecisionHandler>(relaxed = true)

        every { token.paymentMethodType }.returns(PaymentMethodType.PAYMENT_CARD.name)
        every { paymentMethodRepository.getPaymentMethod() }.returns(token)

        every { paymentResult.paymentStatus }.returns(PaymentStatus.FAILED)

        runTest {
            paymentResultEventsResolver.resolve(paymentResult, resumeHandler)
        }

        val event = slot<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assertEquals(CheckoutEventType.CHECKOUT_AUTO_ERROR, event.captured.type)

        val paymentError = event.captured as CheckoutEvent.CheckoutPaymentError
        assertEquals(
            paymentResult.payment,
            paymentError.data?.payment
        )

        assertEquals(
            paymentError.error,
            PaymentError.PaymentFailedError(
                paymentResult.payment.id,
                paymentResult.paymentStatus,
                paymentMethodRepository.getPaymentMethod().paymentMethodType.orEmpty()
            )
        )
    }

    @Test
    fun `resolve() should dispatch PAYMENT_SUCCESS type when payment status is SUCCESS`() {
        val paymentResult = mockk<PaymentResult>(relaxed = true)
        val resumeHandler = mockk<PrimerResumeDecisionHandler>(relaxed = true)

        every { paymentResult.paymentStatus }.returns(PaymentStatus.SUCCESS)

        runTest {
            paymentResultEventsResolver.resolve(paymentResult, resumeHandler)
        }

        val event = slot<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(event)) }
        verify { resumeHandler.handleSuccess() }

        assertEquals(CheckoutEventType.PAYMENT_SUCCESS, event.captured.type)
        assertEquals(
            paymentResult.payment,
            (event.captured as CheckoutEvent.PaymentSuccess).data.payment
        )
    }

    @Test
    fun `resolve() should dispatch PAYMENT_SUCCESS type when payment status is PENDING and has showSuccessCheckoutOnPendingPayment flag`() {
        val paymentResult = mockk<PaymentResult>(relaxed = true)
        val resumeHandler = mockk<PrimerResumeDecisionHandler>(relaxed = true)

        every { paymentResult.paymentStatus }.returns(PaymentStatus.PENDING)
        every { paymentResult.showSuccessCheckoutOnPendingPayment }.returns(true)

        runTest {
            paymentResultEventsResolver.resolve(paymentResult, resumeHandler)
        }

        val event = slot<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(event)) }
        verify { resumeHandler.handleSuccess() }

        assertEquals(CheckoutEventType.PAYMENT_SUCCESS, event.captured.type)
        assertEquals(
            paymentResult.payment,
            (event.captured as CheckoutEvent.PaymentSuccess).data.payment
        )
    }
}

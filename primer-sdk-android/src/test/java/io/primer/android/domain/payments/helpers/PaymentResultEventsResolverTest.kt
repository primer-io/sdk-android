package io.primer.android.domain.payments.helpers

import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import io.primer.android.InstantExecutorExtension
import io.primer.android.completion.ResumeDecisionHandler
import io.primer.android.data.payments.create.models.PaymentStatus
import io.primer.android.domain.payments.create.model.PaymentResult
import io.primer.android.events.CheckoutEvent
import io.primer.android.events.CheckoutEventType
import io.primer.android.events.EventDispatcher
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(InstantExecutorExtension::class, MockKExtension::class)
@ExperimentalCoroutinesApi
internal class PaymentResultEventsResolverTest {

    @RelaxedMockK
    internal lateinit var eventDispatcher: EventDispatcher

    private lateinit var paymentResultEventsResolver: PaymentResultEventsResolver

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this, relaxed = true)
        paymentResultEventsResolver =
            PaymentResultEventsResolver(
                eventDispatcher,
            )
    }

    @Test
    fun `resolve() should call handleNewClientToken when payment status is PENDING`() {
        val paymentResult = mockk<PaymentResult>(relaxed = true)
        val resumeHandler = mockk<ResumeDecisionHandler>(relaxed = true)

        every { paymentResult.paymentStatus }.returns(PaymentStatus.PENDING)

        runBlockingTest {
            paymentResultEventsResolver.resolve(paymentResult, resumeHandler)
        }
        val clientToken = slot<String>()

        verify { resumeHandler.handleNewClientToken(capture(clientToken)) }

        assertEquals(paymentResult.clientToken, clientToken.captured)
    }

    @Test
    fun `resolve() should dispatch CHECKOUT_AUTO_ERROR type when payment status is FAILED`() {
        val paymentResult = mockk<PaymentResult>(relaxed = true)
        val resumeHandler = mockk<ResumeDecisionHandler>(relaxed = true)

        every { paymentResult.paymentStatus }.returns(PaymentStatus.FAILED)

        runBlockingTest {
            paymentResultEventsResolver.resolve(paymentResult, resumeHandler)
        }
        val event = slot<CheckoutEvent>()

        verify { eventDispatcher.dispatchEvent(capture(event)) }

        assertEquals(CheckoutEventType.CHECKOUT_AUTO_ERROR, event.captured.type)
        assertEquals(
            paymentResult.payment,
            (event.captured as CheckoutEvent.CheckoutPaymentError).data?.payment
        )
    }

    @Test
    fun `resolve() should dispatch PAYMENT_SUCCESS type when payment status is SUCCESS`() {
        val paymentResult = mockk<PaymentResult>(relaxed = true)
        val resumeHandler = mockk<ResumeDecisionHandler>(relaxed = true)

        every { paymentResult.paymentStatus }.returns(PaymentStatus.SUCCESS)

        runBlockingTest {
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

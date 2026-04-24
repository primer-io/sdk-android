package io.primer.checkout.orchestrator.domain

import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.errors.domain.model.PaymentError
import io.primer.checkout.orchestrator.domain.model.CheckoutDecision
import io.primer.statetransport.domain.model.CheckoutOutcome
import io.primer.statetransport.domain.model.ClientInstructions
import io.primer.statetransport.domain.model.PaymentInfo
import io.primer.statetransport.domain.model.PaymentStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class CheckoutDecisionResolverTest {

    @RelaxedMockK
    lateinit var logReporter: LogReporter

    private lateinit var resolver: CheckoutDecisionResolver

    @BeforeEach
    fun setUp() {
        resolver = CheckoutDecisionResolver(logReporter)
    }

    @Test
    fun `resolve should return Success with payment when outcome is CHECKOUT_COMPLETE`() {
        val end = ClientInstructions.End(
            checkoutOutcome = CheckoutOutcome.CHECKOUT_COMPLETE,
            payment = createPaymentInfo(),
        )

        val decision = resolver.resolve(end, PAYMENT_METHOD_TYPE)

        assertTrue(decision is CheckoutDecision.Success)
        assertEquals(Payment(id = PAYMENT_ID, orderId = ORDER_ID), decision.payment)
    }

    @Test
    fun `resolve should return Success with null payment when outcome is CHECKOUT_COMPLETE and payment is null`() {
        val end = ClientInstructions.End(
            checkoutOutcome = CheckoutOutcome.CHECKOUT_COMPLETE,
            payment = null,
        )

        val decision = resolver.resolve(end, PAYMENT_METHOD_TYPE)

        assertTrue(decision is CheckoutDecision.Success)
        assertNull(decision.payment)
    }

    @Test
    fun `resolve should return Failure with PaymentFailedError when outcome is CHECKOUT_FAILURE`() {
        val end = ClientInstructions.End(
            checkoutOutcome = CheckoutOutcome.CHECKOUT_FAILURE,
            payment = createPaymentInfo(status = PaymentStatus.FAILED),
        )

        val decision = resolver.resolve(end, PAYMENT_METHOD_TYPE)

        assertTrue(decision is CheckoutDecision.Failure)
        val failure = decision as CheckoutDecision.Failure
        assertEquals(Payment(id = PAYMENT_ID, orderId = ORDER_ID), failure.payment)
        val error = failure.error as PaymentError.PaymentFailedError
        assertEquals(PAYMENT_ID, error.paymentId)
        assertEquals(PAYMENT_METHOD_TYPE, error.paymentMethodType)
    }

    @Test
    fun `resolve should throw when outcome is CHECKOUT_FAILURE and payment is null`() {
        val end = ClientInstructions.End(
            checkoutOutcome = CheckoutOutcome.CHECKOUT_FAILURE,
            payment = null,
        )

        assertThrows(IllegalArgumentException::class.java) {
            resolver.resolve(end, PAYMENT_METHOD_TYPE)
        }
    }

    @Test
    fun `resolve should return Failure when outcome is DETERMINE_FROM_PAYMENT_STATUS and status is FAILED`() {
        val end = ClientInstructions.End(
            checkoutOutcome = CheckoutOutcome.DETERMINE_FROM_PAYMENT_STATUS,
            payment = createPaymentInfo(status = PaymentStatus.FAILED),
        )

        val decision = resolver.resolve(end, PAYMENT_METHOD_TYPE)

        assertTrue(decision is CheckoutDecision.Failure)
        val failure = decision as CheckoutDecision.Failure
        assertEquals(Payment(id = PAYMENT_ID, orderId = ORDER_ID), failure.payment)
        val error = failure.error as PaymentError.PaymentFailedError
        assertEquals(PAYMENT_ID, error.paymentId)
        assertEquals(PAYMENT_METHOD_TYPE, error.paymentMethodType)
    }

    @Test
    fun `resolve should return Success when outcome is DETERMINE_FROM_PAYMENT_STATUS and status is SUCCESS`() {
        val end = ClientInstructions.End(
            checkoutOutcome = CheckoutOutcome.DETERMINE_FROM_PAYMENT_STATUS,
            payment = createPaymentInfo(status = PaymentStatus.SUCCESS),
        )

        val decision = resolver.resolve(end, PAYMENT_METHOD_TYPE)

        assertTrue(decision is CheckoutDecision.Success)
        assertEquals(Payment(id = PAYMENT_ID, orderId = ORDER_ID), decision.payment)
    }

    @Test
    fun `resolve should return Success when outcome is DETERMINE_FROM_PAYMENT_STATUS and status is PENDING`() {
        val end = ClientInstructions.End(
            checkoutOutcome = CheckoutOutcome.DETERMINE_FROM_PAYMENT_STATUS,
            payment = createPaymentInfo(status = PaymentStatus.PENDING),
        )

        val decision = resolver.resolve(end, PAYMENT_METHOD_TYPE)

        assertTrue(decision is CheckoutDecision.Success)
    }

    @Test
    fun `resolve should return Success when outcome is null and payment is null`() {
        val end = ClientInstructions.End(
            checkoutOutcome = null,
            payment = null,
        )

        val decision = resolver.resolve(end, PAYMENT_METHOD_TYPE)

        assertTrue(decision is CheckoutDecision.Success)
        assertNull(decision.payment)
    }

    @Test
    fun `resolve should log the checkout outcome`() {
        val end = ClientInstructions.End(
            checkoutOutcome = CheckoutOutcome.CHECKOUT_COMPLETE,
            payment = null,
        )

        resolver.resolve(end, PAYMENT_METHOD_TYPE)

        verify { logReporter.info("Checkout ended with outcome: ${CheckoutOutcome.CHECKOUT_COMPLETE}.") }
    }

    private fun createPaymentInfo(status: PaymentStatus = PaymentStatus.SUCCESS) = PaymentInfo(
        id = PAYMENT_ID,
        date = "2026-04-06",
        amount = 1000L,
        currencyCode = "USD",
        status = status,
        customerId = null,
        orderId = ORDER_ID,
    )

    private companion object {
        const val PAYMENT_METHOD_TYPE = "ADYEN_IDEAL"
        const val PAYMENT_ID = "pay_123"
        const val ORDER_ID = "order_456"
    }
}

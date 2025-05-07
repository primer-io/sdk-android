package io.primer.android.payments.core.helpers

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.create.data.model.CheckoutOutcome
import io.primer.android.payments.core.create.data.model.PaymentStatus
import io.primer.android.payments.core.create.data.model.RequiredActionName
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.core.create.domain.model.PaymentResult
import io.primer.android.payments.core.errors.domain.model.PaymentError
import io.primer.android.payments.core.tokenization.data.model.PaymentMethodTokenInternal
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class PaymentDecisionResolverTest {
    private lateinit var tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository
    private lateinit var logReporter: LogReporter
    private lateinit var paymentDecisionResolver: PaymentDecisionResolver

    @BeforeEach
    fun setup() {
        tokenizedPaymentMethodRepository = mockk()
        logReporter = mockk(relaxed = true)
        paymentDecisionResolver = PaymentDecisionResolver(tokenizedPaymentMethodRepository, logReporter)
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `resolve should return Success when checkoutOutcome is CHECKOUT_COMPLETE`() {
        val paymentResult = PaymentResult(
            payment = Payment("payment123", "order456"),
            paymentStatus = PaymentStatus.PENDING,
            requiredActionName = null,
            clientToken = null,
            checkoutOutcome = CheckoutOutcome.CHECKOUT_COMPLETE,
        )

        val decision = paymentDecisionResolver.resolve(paymentResult)

        assertEquals(PaymentDecision.Success(paymentResult.payment), decision)
        verify { logReporter.info("Received new payment status: ${PaymentStatus.PENDING}.") }
    }

    @Test
    fun `resolve should return Error when checkoutOutcome is CHECKOUT_FAILURE`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>()
        every { paymentMethodToken.paymentMethodType } returns "credit_card"
        every { tokenizedPaymentMethodRepository.getPaymentMethod() } returns paymentMethodToken

        val paymentResult = PaymentResult(
            payment = Payment("payment456", "order789"),
            paymentStatus = PaymentStatus.SUCCESS, // even if SUCCESS, checkoutOutcome overrides
            requiredActionName = null,
            clientToken = null,
            checkoutOutcome = CheckoutOutcome.CHECKOUT_FAILURE,
        )

        val decision = paymentDecisionResolver.resolve(paymentResult)

        assertEquals(
            PaymentDecision.Error(
                PaymentError.PaymentFailedError("payment456", PaymentStatus.SUCCESS, "credit_card"),
                paymentResult.payment,
            ),
            decision,
        )
        verify { logReporter.info("Received new payment status: ${PaymentStatus.SUCCESS}.") }
    }

    @Test
    fun `resolve should return Pending decision when PaymentStatus is PENDING`() {
        // Arrange
        val paymentResult =
            PaymentResult(
                payment = Payment("payment123", "order456"),
                paymentStatus = PaymentStatus.PENDING,
                requiredActionName = RequiredActionName.USE_PRIMER_SDK,
                clientToken = "clientToken123",
                checkoutOutcome = null,
            )

        // Act
        val decision = paymentDecisionResolver.resolve(paymentResult)

        // Assert
        assertEquals(PaymentDecision.Pending("clientToken123", paymentResult.payment), decision)
        verify { logReporter.info("Received new payment status: ${PaymentStatus.PENDING}.") }
        verify { logReporter.debug("Handling required action: USE_PRIMER_SDK for payment id: payment123") }
    }

    @Test
    fun `resolve should return Error decision when PaymentStatus is FAILED`() {
        // Arrange
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>()
        every { paymentMethodToken.paymentMethodType } returns "credit_card"
        every { tokenizedPaymentMethodRepository.getPaymentMethod() } returns paymentMethodToken
        val paymentResult =
            PaymentResult(
                payment = Payment("payment456", "order789"),
                paymentStatus = PaymentStatus.FAILED,
                requiredActionName = null,
                clientToken = null,
                checkoutOutcome = null,
            )

        // Act
        val decision = paymentDecisionResolver.resolve(paymentResult)

        // Assert
        assertEquals(
            PaymentDecision.Error(
                PaymentError.PaymentFailedError("payment456", PaymentStatus.FAILED, "credit_card"),
                paymentResult.payment,
            ),
            decision,
        )
        verify { logReporter.info("Received new payment status: ${PaymentStatus.FAILED}.") }
        verify(exactly = 0) { logReporter.debug(any()) } // No debug log expected for FAILED status
    }

    @Test
    fun `resolve should return Success decision for other PaymentStatus`() {
        // Arrange
        val paymentResult =
            PaymentResult(
                payment = Payment("payment789", "order012"),
                paymentStatus = PaymentStatus.SUCCESS,
                requiredActionName = null,
                clientToken = null,
                checkoutOutcome = null,
            )

        // Act
        val decision = paymentDecisionResolver.resolve(paymentResult)

        // Assert
        assertEquals(PaymentDecision.Success(paymentResult.payment), decision)
        verify { logReporter.info("Received new payment status: ${PaymentStatus.SUCCESS}.") }
        verify(exactly = 0) { logReporter.debug(any()) } // No debug log expected for SUCCESS status
    }

    @Test
    fun `resolve should return Success when checkoutOutcome is DETERMINE_FROM_PAYMENT_STATUS and status is SUCCESS`() {
        val paymentResult = PaymentResult(
            payment = Payment("payment789", "order012"),
            paymentStatus = PaymentStatus.SUCCESS,
            requiredActionName = null,
            clientToken = null,
            checkoutOutcome = CheckoutOutcome.DETERMINE_FROM_PAYMENT_STATUS,
        )

        val decision = paymentDecisionResolver.resolve(paymentResult)

        assertEquals(PaymentDecision.Success(paymentResult.payment), decision)
        verify { logReporter.info("Received new payment status: ${PaymentStatus.SUCCESS}.") }
        verify(exactly = 0) { logReporter.debug(any()) }
    }

    @Test
    fun `resolve should return Error when checkoutOutcome is DETERMINE_FROM_PAYMENT_STATUS and status is FAILED`() {
        val paymentMethodToken = mockk<PaymentMethodTokenInternal>()
        every { paymentMethodToken.paymentMethodType } returns "credit_card"
        every { tokenizedPaymentMethodRepository.getPaymentMethod() } returns paymentMethodToken

        val paymentResult = PaymentResult(
            payment = Payment("payment456", "order789"),
            paymentStatus = PaymentStatus.FAILED,
            requiredActionName = null,
            clientToken = null,
            checkoutOutcome = CheckoutOutcome.DETERMINE_FROM_PAYMENT_STATUS,
        )

        val decision = paymentDecisionResolver.resolve(paymentResult)

        assertEquals(
            PaymentDecision.Error(
                PaymentError.PaymentFailedError("payment456", PaymentStatus.FAILED, "credit_card"),
                paymentResult.payment,
            ),
            decision,
        )
        verify { logReporter.info("Received new payment status: ${PaymentStatus.FAILED}.") }
        verify(exactly = 0) { logReporter.debug(any()) }
    }
}

package io.primer.android.payments.core.helpers

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.core.logging.internal.LogReporter
import io.primer.android.payments.core.create.data.model.PaymentStatus
import io.primer.android.payments.core.create.data.model.RequiredActionName
import io.primer.android.domain.payments.create.model.Payment
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
    fun `resolve should return Pending decision when PaymentStatus is PENDING`() {
        // Arrange
        val paymentResult = PaymentResult(
            Payment("payment123", "order456"),
            PaymentStatus.PENDING,
            RequiredActionName.USE_PRIMER_SDK,
            "clientToken123"
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
        val paymentResult = PaymentResult(
            Payment("payment456", "order789"),
            PaymentStatus.FAILED,
            null,
            null
        )

        // Act
        val decision = paymentDecisionResolver.resolve(paymentResult)

        // Assert
        assertEquals(
            PaymentDecision.Error(
                PaymentError.PaymentFailedError("payment456", PaymentStatus.FAILED, "credit_card"),
                paymentResult.payment
            ),
            decision
        )
        verify { logReporter.info("Received new payment status: ${PaymentStatus.FAILED}.") }
        verify(exactly = 0) { logReporter.debug(any()) } // No debug log expected for FAILED status
    }

    @Test
    fun `resolve should return Success decision for other PaymentStatus`() {
        // Arrange
        val paymentResult = PaymentResult(
            Payment("payment789", "order012"),
            PaymentStatus.SUCCESS,
            null,
            null
        )

        // Act
        val decision = paymentDecisionResolver.resolve(paymentResult)

        // Assert
        assertEquals(PaymentDecision.Success(paymentResult.payment), decision)
        verify { logReporter.info("Received new payment status: ${PaymentStatus.SUCCESS}.") }
        verify(exactly = 0) { logReporter.debug(any()) } // No debug log expected for SUCCESS status
    }
}

package io.primer.android.vouchers.multibanco.implementation.payment.delegate

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import io.primer.android.data.settings.PrimerPaymentHandling
import io.primer.android.data.settings.internal.PrimerConfig
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.helpers.ManualFlowSuccessHandler
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.payments.core.resume.domain.handler.PendingResumeHandler
import io.primer.android.vouchers.multibanco.MultibancoCheckoutAdditionalInfo
import io.primer.android.vouchers.multibanco.implementation.payment.resume.handler.MultibancoDecision
import io.primer.android.vouchers.multibanco.implementation.payment.resume.handler.MultibancoResumeHandler
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MultibancoPaymentDelegateTest {
    private lateinit var paymentMethodTokenHandler: PaymentMethodTokenHandler
    private lateinit var resumePaymentHandler: PaymentResumeHandler
    private lateinit var config: PrimerConfig
    private lateinit var successHandler: CheckoutSuccessHandler
    private lateinit var manualFlowSuccessHandler: ManualFlowSuccessHandler
    private lateinit var pendingResumeHandler: PendingResumeHandler
    private lateinit var errorHandler: CheckoutErrorHandler
    private lateinit var baseErrorResolver: BaseErrorResolver
    private lateinit var resumeHandler: MultibancoResumeHandler

    private lateinit var multibancoPaymentDelegate: MultibancoPaymentDelegate

    @BeforeEach
    fun setUp() {
        paymentMethodTokenHandler = mockk()
        resumePaymentHandler = mockk()
        config = mockk()
        successHandler = mockk()
        manualFlowSuccessHandler = mockk()
        pendingResumeHandler = mockk()
        errorHandler = mockk()
        baseErrorResolver = mockk()
        resumeHandler = mockk()

        multibancoPaymentDelegate = MultibancoPaymentDelegate(
            paymentMethodTokenHandler,
            resumePaymentHandler,
            config,
            successHandler,
            manualFlowSuccessHandler,
            pendingResumeHandler,
            errorHandler,
            baseErrorResolver,
            resumeHandler
        )
    }

    @Test
    fun `handleNewClientToken should handle token and return success when in auto payment mode`() = runTest {
        // Arrange
        every { config.settings.paymentHandling } returns PrimerPaymentHandling.AUTO
        val clientToken = "client_token"
        val payment = mockk<Payment>()
        val decision = mockk<MultibancoDecision> {
            every { expiresAt } returns "expires_at"
            every { reference } returns "reference"
            every { entity } returns "entity"
        }
        val result = Result.success(decision)

        coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns result
        coEvery { successHandler.handle(payment, any()) } just Runs

        // Act
        val handleNewClientTokenResult = multibancoPaymentDelegate.handleNewClientToken(clientToken, payment)

        // Assert
        assertTrue(handleNewClientTokenResult.isSuccess)
        coVerify {
            successHandler.handle(
                payment = payment,
                additionalInfo = MultibancoCheckoutAdditionalInfo("expires_at", "reference", "entity")
            )
        }
        verify { config.settings.paymentHandling }
        coVerify(exactly = 0) {
            manualFlowSuccessHandler.handle(any())
            pendingResumeHandler.handle(any())
        }
    }

    @Test
    fun `handleNewClientToken should handle token and return failure when in auto payment mode`() = runTest {
        // Arrange
        val clientToken = "client_token"
        val payment = mockk<Payment>()
        val exception = Exception("An error occurred")
        val result = Result.failure<MultibancoDecision>(exception)

        coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns result

        // Act
        val handleNewClientTokenResult = multibancoPaymentDelegate.handleNewClientToken(clientToken, payment)

        // Assert
        assertTrue(handleNewClientTokenResult.isFailure)
        coVerify(exactly = 0) {
            successHandler.handle(any(), any())
            manualFlowSuccessHandler.handle(any())
            pendingResumeHandler.handle(any())
        }
        verify(exactly = 0) { config.settings.paymentHandling }
    }

    @Test
    fun `handleNewClientToken should handle token and return success when in manual payment mode`() = runTest {
        // Arrange
        every { config.settings.paymentHandling } returns PrimerPaymentHandling.MANUAL
        val clientToken = "client_token"
        val payment = mockk<Payment>()
        val decision = mockk<MultibancoDecision> {
            every { expiresAt } returns "expires_at"
            every { reference } returns "reference"
            every { entity } returns "entity"
        }
        val result = Result.success(decision)

        coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns result
        coEvery { manualFlowSuccessHandler.handle(any()) } just Runs
        coEvery { pendingResumeHandler.handle(any()) } returns mockk()

        // Act
        val handleNewClientTokenResult = multibancoPaymentDelegate.handleNewClientToken(clientToken, payment)

        // Assert
        assertTrue(handleNewClientTokenResult.isSuccess)
        coVerify {
            manualFlowSuccessHandler.handle(MultibancoCheckoutAdditionalInfo("expires_at", "reference", "entity"))
            pendingResumeHandler.handle(MultibancoCheckoutAdditionalInfo("expires_at", "reference", "entity"))
        }
        coVerify(exactly = 0) { successHandler.handle(any(), any()) }
        verify { config.settings.paymentHandling }
    }

    @Test
    fun `handleNewClientToken should handle token and return failure when in manual payment mode`() = runTest {
        // Arrange
        val clientToken = "client_token"
        val payment = mockk<Payment>()
        val exception = Exception("An error occurred")
        val result = Result.failure<MultibancoDecision>(exception)

        coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns result

        // Act
        val handleNewClientTokenResult = multibancoPaymentDelegate.handleNewClientToken(clientToken, payment)

        // Assert
        assertTrue(handleNewClientTokenResult.isFailure)
        coVerify(exactly = 0) {
            successHandler.handle(any(), any())
            manualFlowSuccessHandler.handle(any())
            pendingResumeHandler.handle(any())
        }
        verify(exactly = 0) { config.settings.paymentHandling }
    }
}

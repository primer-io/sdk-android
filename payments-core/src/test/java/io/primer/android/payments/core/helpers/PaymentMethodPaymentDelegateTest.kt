package io.primer.android.payments.core.helpers

import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import io.primer.android.PrimerSessionIntent
import io.primer.android.domain.error.models.PrimerError
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.domain.tokenization.models.PrimerPaymentMethodTokenData
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.payments.core.create.data.model.PaymentStatus
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.payments.core.create.domain.model.PaymentDecision
import io.primer.android.payments.core.errors.domain.model.PaymentError
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PaymentMethodPaymentDelegateTest {
    private lateinit var paymentMethodTokenHandler: PaymentMethodTokenHandler
    private lateinit var resumePaymentHandler: PaymentResumeHandler
    private lateinit var successHandler: CheckoutSuccessHandler
    private lateinit var errorHandler: CheckoutErrorHandler
    private lateinit var baseErrorResolver: BaseErrorResolver

    private lateinit var paymentMethodPaymentDelegate: PaymentMethodPaymentDelegate

    @BeforeEach
    fun setup() {
        paymentMethodTokenHandler = mockk()
        resumePaymentHandler = mockk()
        successHandler = mockk()
        errorHandler = mockk()
        baseErrorResolver = mockk()

        paymentMethodPaymentDelegate =
            spyk(
                object : PaymentMethodPaymentDelegate(
                    paymentMethodTokenHandler = paymentMethodTokenHandler,
                    resumePaymentHandler = resumePaymentHandler,
                    successHandler = successHandler,
                    errorHandler = errorHandler,
                    baseErrorResolver = baseErrorResolver,
                ) {
                    override suspend fun handleNewClientToken(
                        clientToken: String,
                        payment: Payment?,
                    ): Result<Unit> {
                        // Mock implementation for testing
                        return Result.success(Unit)
                    }
                },
            )
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `handlePaymentMethodToken with PaymentDecision_Error should call appropriate handlers based on PaymentDecision`() {
        // Arrange
        val paymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>(relaxed = true)
        val intent = PrimerSessionIntent.CHECKOUT
        val anError = PaymentError.PaymentFailedError("payment789", PaymentStatus.FAILED, "credit_card")
        val aPayment = Payment("payment123", "order456")
        val paymentDecision = PaymentDecision.Error(anError, aPayment)

        coEvery {
            paymentMethodTokenHandler.handle(paymentMethodTokenData, intent)
        } returns Result.success(paymentDecision)
        coEvery { errorHandler.handle(any(), any()) } just Runs

        // Act
        runTest { paymentMethodPaymentDelegate.handlePaymentMethodToken(paymentMethodTokenData, intent) }

        // Assert
        coVerify { paymentMethodTokenHandler.handle(paymentMethodTokenData, intent) }
        coVerify { errorHandler.handle(paymentDecision.error, paymentDecision.payment) }
        coVerify(exactly = 0) { successHandler.handle(any(), any()) }
        coVerify(exactly = 0) { paymentMethodPaymentDelegate.handleNewClientToken(any(), any()) }
    }

    @Test
    fun `handlePaymentMethodToken with PaymentDecision_Pending should call appropriate handlers based on PaymentDecision`() {
        // Arrange
        val paymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>(relaxed = true)
        val intent = PrimerSessionIntent.CHECKOUT
        val clientToken = "clientToken"
        val aPayment = Payment("payment123", "order456")
        val paymentDecision = PaymentDecision.Pending(clientToken, aPayment)

        coEvery {
            paymentMethodTokenHandler.handle(paymentMethodTokenData, intent)
        } returns Result.success(paymentDecision)
        coEvery { errorHandler.handle(any(), any()) } just Runs

        // Act
        runTest { paymentMethodPaymentDelegate.handlePaymentMethodToken(paymentMethodTokenData, intent) }

        // Assert
        coVerify { paymentMethodTokenHandler.handle(paymentMethodTokenData, intent) }
        coVerify { paymentMethodPaymentDelegate.handleNewClientToken(clientToken, aPayment) }
        coVerify(exactly = 0) { errorHandler.handle(any(), any()) }
        coVerify(exactly = 0) { successHandler.handle(any(), any()) }
    }

    @Test
    fun `handlePaymentMethodToken with PaymentDecision_Success should call appropriate handlers based on PaymentDecision`() {
        // Arrange
        val paymentMethodTokenData = mockk<PrimerPaymentMethodTokenData>(relaxed = true)
        val intent = PrimerSessionIntent.CHECKOUT
        val paymentDecision = PaymentDecision.Success(Payment("payment123", "order456"))

        coEvery {
            paymentMethodTokenHandler.handle(paymentMethodTokenData, intent)
        } returns Result.success(paymentDecision)
        coEvery { successHandler.handle(any(), any()) } just Runs

        // Act
        runTest { paymentMethodPaymentDelegate.handlePaymentMethodToken(paymentMethodTokenData, intent) }

        // Assert
        coVerify { paymentMethodTokenHandler.handle(paymentMethodTokenData, intent) }
        coVerify { successHandler.handle(paymentDecision.payment, null) }
        coVerify(exactly = 0) { errorHandler.handle(any(), any()) }
        coVerify(exactly = 0) { paymentMethodPaymentDelegate.handleNewClientToken(any(), any()) }
    }

    @Test
    fun `resumePayment should call appropriate handlers based on PaymentDecision_Error`() {
        // Arrange
        val resumeToken = "token456"
        val anError = PaymentError.PaymentFailedError("payment789", PaymentStatus.FAILED, "credit_card")
        val aPayment = Payment("payment123", "order456")
        val paymentDecision = PaymentDecision.Error(anError, aPayment)

        coEvery {
            resumePaymentHandler.handle(resumeToken, any())
        } returns Result.success(paymentDecision)
        coEvery { errorHandler.handle(any(), any()) } just Runs

        // Act
        runTest { paymentMethodPaymentDelegate.resumePayment(resumeToken) }

        // Assert
        coVerify { resumePaymentHandler.handle(resumeToken, any()) }
        coVerify { errorHandler.handle(anError, aPayment) }
        coVerify(exactly = 0) { successHandler.handle(any(), any()) }
        coVerify(exactly = 0) { paymentMethodPaymentDelegate.handleNewClientToken(any(), any()) }
    }

    @Test
    fun `resumePayment should call appropriate handlers when PaymentDecision_Pending`() {
        // Arrange
        val resumeToken = "token456"
        val paymentDecision = PaymentDecision.Pending("clientToken456", Payment("payment789", "order012"))

        coEvery {
            resumePaymentHandler.handle(resumeToken, any())
        } returns Result.success(paymentDecision)
        coEvery { successHandler.handle(any(), any()) } just Runs

        // Act
        runTest { paymentMethodPaymentDelegate.resumePayment(resumeToken) }

        // Assert
        coVerify { resumePaymentHandler.handle(resumeToken, any()) }
        coVerify { paymentMethodPaymentDelegate.handleNewClientToken(any(), any()) }
        coVerify(exactly = 0) { successHandler.handle(paymentDecision.payment!!, null) }
        coVerify(exactly = 0) { errorHandler.handle(any(), any()) }
    }

    @Test
    fun `resumePayment should call appropriate handlers when PaymentDecision_Success`() {
        // Arrange
        val resumeToken = "token456"
        val paymentDecision = PaymentDecision.Success(Payment("payment789", "order012"))

        coEvery {
            resumePaymentHandler.handle(resumeToken, any())
        } returns Result.success(paymentDecision)
        coEvery { successHandler.handle(any(), any()) } just Runs

        // Act
        runTest { paymentMethodPaymentDelegate.resumePayment(resumeToken) }

        // Assert
        coVerify { resumePaymentHandler.handle(resumeToken, any()) }
        coVerify { successHandler.handle(paymentDecision.payment, null) }
        coVerify(exactly = 0) { errorHandler.handle(any(), any()) }
        coVerify(exactly = 0) { paymentMethodPaymentDelegate.handleNewClientToken(any(), any()) }
    }

    @Test
    fun `resumePayment should call appropriate handlers when failure is returned`() {
        // Arrange
        val resumeToken = "token456"
        val exception = Exception()
        val error = mockk<PrimerError>()

        coEvery {
            resumePaymentHandler.handle(resumeToken, any())
        } returns Result.failure(exception)
        coEvery { baseErrorResolver.resolve(any()) } returns error
        coEvery { errorHandler.handle(any(), any()) } just Runs

        // Act
        runTest { paymentMethodPaymentDelegate.resumePayment(resumeToken) }

        // Assert
        coVerify { resumePaymentHandler.handle(resumeToken, any()) }
        coVerify { errorHandler.handle(error, null) }
        coVerify(exactly = 0) { successHandler.handle(any(), any()) }
        coVerify(exactly = 0) { paymentMethodPaymentDelegate.handleNewClientToken(any(), any()) }
    }

    @Test
    fun `handleError should call errorHandler with resolved error and a payment`() {
        // Arrange
        val throwable = RuntimeException("Test error")
        val resolvedError = PaymentError.PaymentFailedError("payment789", PaymentStatus.FAILED, "credit_card")

        every { baseErrorResolver.resolve(throwable) } returns resolvedError
        coEvery { errorHandler.handle(any(), any()) } just Runs

        // Act
        runTest {
            paymentMethodPaymentDelegate.handleError(throwable)
        }

        // Assert
        verify { baseErrorResolver.resolve(throwable) }
        coVerify { errorHandler.handle(resolvedError, any()) }
    }
}

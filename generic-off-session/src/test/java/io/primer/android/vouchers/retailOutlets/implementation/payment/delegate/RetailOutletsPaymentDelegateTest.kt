package io.primer.android.vouchers.retailOutlets.implementation.payment.delegate

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.vouchers.retailOutlets.implementation.payment.resume.handler.RetailOutletsDecision
import io.primer.android.vouchers.retailOutlets.implementation.payment.resume.handler.RetailOutletsResumeHandler
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RetailOutletsPaymentDelegateTest {
    private lateinit var paymentMethodTokenHandler: PaymentMethodTokenHandler
    private lateinit var resumePaymentHandler: PaymentResumeHandler
    private lateinit var successHandler: CheckoutSuccessHandler
    private lateinit var errorHandler: CheckoutErrorHandler
    private lateinit var baseErrorResolver: BaseErrorResolver
    private lateinit var resumeHandler: RetailOutletsResumeHandler

    private lateinit var retailOutletsPaymentDelegate: RetailOutletsPaymentDelegate

    @BeforeEach
    fun setUp() {
        paymentMethodTokenHandler = mockk()
        resumePaymentHandler = mockk()
        successHandler = mockk()
        errorHandler = mockk()
        baseErrorResolver = mockk()
        resumeHandler = mockk()

        retailOutletsPaymentDelegate =
            RetailOutletsPaymentDelegate(
                paymentMethodTokenHandler,
                resumePaymentHandler,
                successHandler,
                errorHandler,
                baseErrorResolver,
                resumeHandler,
            )
    }

    @AfterEach
    fun tearDown() {
        confirmVerified(
            paymentMethodTokenHandler,
            resumePaymentHandler,
            successHandler,
            errorHandler,
            baseErrorResolver,
            resumeHandler,
        )
    }

    @Test
    fun `handleNewClientToken should handle token and return success`() =
        runTest {
            // Arrange
            val clientToken = "client_token"
            val payment = mockk<Payment>()
            val decision =
                mockk<RetailOutletsDecision> {
                    every { expiresAt } returns "expires_at"
                    every { reference } returns "reference"
                    every { retailerName } returns "retailerName"
                }
            val result = Result.success(decision)

            coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns result

            // Act
            val handleNewClientTokenResult = retailOutletsPaymentDelegate.handleNewClientToken(clientToken, payment)

            // Assert
            coVerify { resumeHandler.continueWithNewClientToken(clientToken) }
            assertTrue(handleNewClientTokenResult.isSuccess)
        }

    @Test
    fun `handleNewClientToken should handle token and return failure`() =
        runTest {
            // Arrange
            val clientToken = "client_token"
            val payment = mockk<Payment>()
            val exception = Exception("An error occurred")
            val result = Result.failure<RetailOutletsDecision>(exception)

            coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns result

            // Act
            val handleNewClientTokenResult = retailOutletsPaymentDelegate.handleNewClientToken(clientToken, payment)

            // Assert
            coVerify { resumeHandler.continueWithNewClientToken(clientToken) }
            assertTrue(handleNewClientTokenResult.isFailure)
        }
}

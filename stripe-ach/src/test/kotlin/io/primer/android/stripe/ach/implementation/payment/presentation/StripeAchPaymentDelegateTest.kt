package io.primer.android.stripe.ach.implementation.payment.presentation

import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.stripe.ach.implementation.payment.resume.handler.StripeAchDecision
import io.primer.android.stripe.ach.implementation.payment.resume.handler.StripeAchResumeDecisionHandler
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class StripeAchPaymentDelegateTest {
    @MockK
    private lateinit var paymentMethodTokenHandler: PaymentMethodTokenHandler

    @MockK
    private lateinit var resumePaymentHandler: PaymentResumeHandler

    @MockK
    private lateinit var successHandler: CheckoutSuccessHandler

    @MockK
    private lateinit var errorHandler: CheckoutErrorHandler

    @MockK
    private lateinit var baseErrorResolver: BaseErrorResolver

    @MockK
    private lateinit var resumeHandler: StripeAchResumeDecisionHandler

    @InjectMockKs
    private lateinit var delegate: StripeAchPaymentDelegate

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
    fun `handleNewClientToken should handle token and return success and update lastDecision`() =
        runTest {
            // Arrange
            val clientToken = "clientToken"
            val payment = mockk<Payment>()
            val decision =
                mockk<StripeAchDecision> {
                    every { sdkCompleteUrl } returns "sdkCompleteUrl"
                    every { stripePaymentIntentId } returns "stripePaymentIntentId"
                    every { stripeClientSecret } returns "stripeClientSecret"
                }
            val result = Result.success(decision)

            coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns result

            // Act
            val handleNewClientTokenResult = delegate.handleNewClientToken(clientToken, payment)

            // Assert
            coVerify { resumeHandler.continueWithNewClientToken(clientToken) }
            assertEquals(decision, delegate.lastDecision)
            assertTrue(handleNewClientTokenResult.isSuccess)
        }

    @Test
    fun `handleNewClientToken should handle token and return failure`() =
        runTest {
            // Arrange
            val clientToken = "clientToken"
            val payment = mockk<Payment>()
            val exception = Exception("An error occurred")
            val result = Result.failure<StripeAchDecision>(exception)

            coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns result

            // Act
            val handleNewClientTokenResult = delegate.handleNewClientToken(clientToken, payment)

            // Assert
            coVerify { resumeHandler.continueWithNewClientToken(clientToken) }
            assertTrue(handleNewClientTokenResult.isFailure)
        }
}

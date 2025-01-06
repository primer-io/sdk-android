package io.primer.android.otp.implementation.payment.delegate

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.just
import io.mockk.mockk
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.otp.implementation.payment.resume.handler.OtpDecision
import io.primer.android.otp.implementation.payment.resume.handler.OtpResumeHandler
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.helpers.PollingStartHandler
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class OtpPaymentDelegateTest {
    @MockK
    private lateinit var paymentMethodTokenHandler: PaymentMethodTokenHandler

    @MockK
    private lateinit var resumePaymentHandler: PaymentResumeHandler

    @MockK
    private lateinit var successHandler: CheckoutSuccessHandler

    @MockK
    private lateinit var errorHandler: CheckoutErrorHandler

    @MockK
    private lateinit var pollingStartHandler: PollingStartHandler

    @MockK
    private lateinit var baseErrorResolver: BaseErrorResolver

    @MockK
    private lateinit var resumeHandler: OtpResumeHandler

    @MockK
    private lateinit var tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository

    @InjectMockKs
    private lateinit var delegate: OtpPaymentDelegate

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
    fun `handleNewClientToken should handle token and call polling start handler`() =
        runTest {
            // Arrange
            val clientToken = "clientToken"
            val payment = mockk<Payment>()
            val decision =
                mockk<OtpDecision> {
                    every { statusUrl } returns "statusUrl"
                }
            val paymentMethodType = "paymentMethodType"
            coEvery { tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType } returns paymentMethodType
            coEvery { pollingStartHandler.handle(any()) } just Runs
            val result = Result.success(decision)

            coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns result

            // Act
            val handleNewClientTokenResult = delegate.handleNewClientToken(clientToken, payment)

            // Assert
            coVerify {
                resumeHandler.continueWithNewClientToken(clientToken)
                tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType
                pollingStartHandler.handle(
                    PollingStartHandler.PollingStartData(
                        statusUrl = "statusUrl",
                        paymentMethodType = paymentMethodType,
                    ),
                )
            }
            assertTrue(handleNewClientTokenResult.isSuccess)
        }

    @Test
    fun `handleNewClientToken should handle token and return failure`() =
        runTest {
            // Arrange
            val clientToken = "clientToken"
            val payment = mockk<Payment>()
            val exception = Exception("An error occurred")
            val result = Result.failure<OtpDecision>(exception)

            coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns result

            // Act
            val handleNewClientTokenResult = delegate.handleNewClientToken(clientToken, payment)

            // Assert
            coVerify { resumeHandler.continueWithNewClientToken(clientToken) }
            assertTrue(handleNewClientTokenResult.isFailure)
        }
}

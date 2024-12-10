package io.primer.android.phoneNumber.implementation.payment.delegate

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.helpers.PollingStartHandler
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.phoneNumber.implementation.payment.resume.handler.PhoneNumberDecision
import io.primer.android.phoneNumber.implementation.payment.resume.handler.PhoneNumberResumeHandler
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PhoneNumberPaymentDelegateTest {

    private lateinit var paymentMethodTokenHandler: PaymentMethodTokenHandler
    private lateinit var resumePaymentHandler: PaymentResumeHandler
    private lateinit var successHandler: CheckoutSuccessHandler
    private lateinit var pollingStartHandler: PollingStartHandler
    private lateinit var errorHandler: CheckoutErrorHandler
    private lateinit var baseErrorResolver: BaseErrorResolver
    private lateinit var resumeHandler: PhoneNumberResumeHandler
    private lateinit var tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository

    private lateinit var phoneNumberPaymentDelegate: PhoneNumberPaymentDelegate

    @BeforeEach
    fun setUp() {
        paymentMethodTokenHandler = mockk()
        resumePaymentHandler = mockk()
        successHandler = mockk()
        pollingStartHandler = mockk()
        errorHandler = mockk()
        baseErrorResolver = mockk()
        resumeHandler = mockk()
        tokenizedPaymentMethodRepository = mockk()

        coEvery { pollingStartHandler.handle(any()) } just Runs

        phoneNumberPaymentDelegate = PhoneNumberPaymentDelegate(
            paymentMethodTokenHandler,
            resumePaymentHandler,
            successHandler,
            errorHandler,
            pollingStartHandler,
            baseErrorResolver,
            resumeHandler,
            tokenizedPaymentMethodRepository
        )
    }

    @Test
    fun `handleNewClientToken should handle token and return success`() = runTest {
        // Arrange
        val clientToken = "client_token"
        val payment = mockk<Payment>()
        val decision = mockk<PhoneNumberDecision> {
            every { statusUrl } returns "status_url"
        }
        val result = Result.success(decision)

        every { tokenizedPaymentMethodRepository.getPaymentMethod() } returns mockk {
            every { paymentMethodType } returns "payment_method_type"
        }
        coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns result

        // Act
        val handleNewClientTokenResult = phoneNumberPaymentDelegate.handleNewClientToken(clientToken, payment)

        // Assert
        assertTrue(handleNewClientTokenResult.isSuccess)
        coVerify {
            pollingStartHandler.handle(
                PollingStartHandler.PollingStartData(
                    statusUrl = "status_url",
                    paymentMethodType = "payment_method_type"
                )
            )
        }
    }

    @Test
    fun `handleNewClientToken should handle token and return failure`() = runTest {
        // Arrange
        val clientToken = "client_token"
        val payment = mockk<Payment>()
        val exception = Exception("An error occurred")
        val result = Result.failure<PhoneNumberDecision>(exception)

        coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns result

        // Act
        val handleNewClientTokenResult = phoneNumberPaymentDelegate.handleNewClientToken(clientToken, payment)

        // Assert
        assertTrue(handleNewClientTokenResult.isFailure)
        coVerify(exactly = 0) { successHandler.handle(any(), any()) }
    }
}

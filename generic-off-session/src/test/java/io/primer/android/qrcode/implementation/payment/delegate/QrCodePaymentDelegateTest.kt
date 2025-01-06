package io.primer.android.qrcode.implementation.payment.delegate

import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.errors.domain.BaseErrorResolver
import io.primer.android.payments.core.create.domain.handler.PaymentMethodTokenHandler
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.helpers.CheckoutErrorHandler
import io.primer.android.payments.core.helpers.CheckoutSuccessHandler
import io.primer.android.payments.core.helpers.PollingStartHandler
import io.primer.android.payments.core.resume.domain.handler.PaymentResumeHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.qrcode.QrCodeCheckoutAdditionalInfo
import io.primer.android.qrcode.implementation.payment.resume.handler.QrCodeDecision
import io.primer.android.qrcode.implementation.payment.resume.handler.QrCodeResumeHandler
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class QrCodePaymentDelegateTest {
    private lateinit var paymentMethodTokenHandler: PaymentMethodTokenHandler
    private lateinit var resumePaymentHandler: PaymentResumeHandler
    private lateinit var successHandler: CheckoutSuccessHandler
    private lateinit var additionalInfoHandler: CheckoutAdditionalInfoHandler
    private lateinit var errorHandler: CheckoutErrorHandler
    private lateinit var pollingStartHandler: PollingStartHandler
    private lateinit var baseErrorResolver: BaseErrorResolver
    private lateinit var resumeHandler: QrCodeResumeHandler
    private lateinit var tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository

    private lateinit var qrCodePaymentDelegate: QrCodePaymentDelegate

    @BeforeEach
    fun setUp() {
        paymentMethodTokenHandler = mockk()
        resumePaymentHandler = mockk()
        successHandler = mockk()
        additionalInfoHandler = mockk()
        errorHandler = mockk()
        pollingStartHandler = mockk()
        baseErrorResolver = mockk()
        resumeHandler = mockk()
        tokenizedPaymentMethodRepository = mockk()

        coEvery { pollingStartHandler.handle(any()) } just Runs

        qrCodePaymentDelegate =
            QrCodePaymentDelegate(
                paymentMethodTokenHandler,
                resumePaymentHandler,
                successHandler,
                additionalInfoHandler,
                errorHandler,
                pollingStartHandler,
                baseErrorResolver,
                resumeHandler,
                tokenizedPaymentMethodRepository,
            )
    }

    @Test
    fun `handleNewClientToken should handle token and return success`() =
        runTest {
            // Arrange
            val clientToken = "client_token"
            val payment = mockk<Payment>()
            val decision =
                mockk<QrCodeDecision> {
                    every { statusUrl } returns "status_url"
                    every { expiresAt } returns "expires_at"
                    every { qrCodeUrl } returns "qr_code_url"
                    every { qrCodeBase64 } returns "qr_code_base64"
                }
            val result = Result.success(decision)

            every { tokenizedPaymentMethodRepository.getPaymentMethod() } returns
                mockk {
                    every { paymentMethodType } returns "payment_method_type"
                }
            coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns result
            coEvery { additionalInfoHandler.handle(any()) } just Runs

            // Act
            val handleNewClientTokenResult = qrCodePaymentDelegate.handleNewClientToken(clientToken, payment)

            // Assert
            assertTrue(handleNewClientTokenResult.isSuccess)
            coVerify {
                additionalInfoHandler.handle(any<QrCodeCheckoutAdditionalInfo>())
                pollingStartHandler.handle(
                    PollingStartHandler.PollingStartData(
                        statusUrl = "status_url",
                        paymentMethodType = "payment_method_type",
                    ),
                )
            }
        }

    @Test
    fun `handleNewClientToken should handle token and return failure`() =
        runTest {
            // Arrange
            val clientToken = "client_token"
            val payment = mockk<Payment>()
            val exception = Exception("An error occurred")
            val result = Result.failure<QrCodeDecision>(exception)

            coEvery { resumeHandler.continueWithNewClientToken(clientToken) } returns result

            // Act
            val handleNewClientTokenResult = qrCodePaymentDelegate.handleNewClientToken(clientToken, payment)

            // Assert
            assertTrue(handleNewClientTokenResult.isFailure)
            coVerify(exactly = 0) { successHandler.handle(any(), any()) }
        }
}

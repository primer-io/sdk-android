package io.primer.android.otp.implementation.payment.resume.handler

import io.mockk.every
import io.mockk.mockk
import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.otp.implementation.payment.resume.clientToken.data.OtpClientTokenParser
import io.primer.android.otp.implementation.payment.resume.domain.model.OtpClientToken
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class OtpResumeHandlerTest {
    private lateinit var clientTokenParser: OtpClientTokenParser
    private lateinit var tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository
    private lateinit var validateClientTokenRepository: ValidateClientTokenRepository
    private lateinit var clientTokenRepository: ClientTokenRepository
    private lateinit var checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler
    private lateinit var resumeHandler: OtpResumeHandler

    @BeforeEach
    fun setUp() {
        clientTokenParser = mockk()
        validateClientTokenRepository = mockk()
        clientTokenRepository = mockk()
        tokenizedPaymentMethodRepository = mockk()
        checkoutAdditionalInfoHandler = mockk()

        resumeHandler =
            OtpResumeHandler(
                clientTokenParser,
                tokenizedPaymentMethodRepository,
                validateClientTokenRepository,
                clientTokenRepository,
                checkoutAdditionalInfoHandler,
            )
    }

    @Test
    fun `getResumeDecision should return correct OtpDecision`() =
        runTest {
            // Given
            val clientToken =
                OtpClientToken(
                    statusUrl = "statusUrl",
                    clientTokenIntent = "clientTokenIntent",
                )

            // When
            val result = resumeHandler.getResumeDecision(clientToken)

            // Then
            assertEquals(
                OtpDecision(
                    statusUrl = clientToken.statusUrl,
                ),
                result,
            )
        }

    @Test
    fun `supportedClientTokenIntents should return correct list of intents`() {
        // Given
        every { tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType } returns "ADYEN_BLIK"

        // When
        val result = resumeHandler.supportedClientTokenIntents()

        // Then
        assertEquals(listOf("ADYEN_BLIK_REDIRECTION"), result)
    }

    @Test
    fun `supportedClientTokenIntents should return empty list when paymentMethodType is null`() {
        // Given
        every { tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType } returns null

        // When
        val result = resumeHandler.supportedClientTokenIntents()

        // Then
        assertEquals("_REDIRECTION", result.first())
    }
}

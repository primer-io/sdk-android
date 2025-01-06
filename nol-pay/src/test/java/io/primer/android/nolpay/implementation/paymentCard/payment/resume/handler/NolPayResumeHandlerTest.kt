package io.primer.android.nolpay.implementation.paymentCard.payment.resume.handler

import io.mockk.every
import io.mockk.mockk
import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.nolpay.implementation.paymentCard.payment.resume.clientToken.data.NolPayClientTokenParser
import io.primer.android.nolpay.implementation.paymentCard.payment.resume.clientToken.domain.model.NolPayClientToken
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class NolPayResumeHandlerTest {
    private lateinit var nolPayResumeHandler: NolPayResumeHandler
    private lateinit var clientTokenParser: NolPayClientTokenParser
    private lateinit var validateClientTokenRepository: ValidateClientTokenRepository
    private lateinit var clientTokenRepository: ClientTokenRepository
    private lateinit var tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository
    private lateinit var checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler

    @BeforeEach
    fun setUp() {
        clientTokenParser = mockk()
        validateClientTokenRepository = mockk()
        clientTokenRepository = mockk()
        tokenizedPaymentMethodRepository = mockk()
        checkoutAdditionalInfoHandler = mockk()

        nolPayResumeHandler =
            NolPayResumeHandler(
                clientTokenParser,
                validateClientTokenRepository,
                clientTokenRepository,
                checkoutAdditionalInfoHandler,
                tokenizedPaymentMethodRepository,
            )
    }

    @Test
    fun `getResumeDecision should return correct NolPayResumeDecision`() =
        runTest {
            // Given
            val clientToken =
                NolPayClientToken(
                    clientTokenIntent = "intent",
                    transactionNumber = "transaction123",
                    statusUrl = "http://status.url",
                    completeUrl = "http://complete.url",
                )

            // When
            val result = nolPayResumeHandler.getResumeDecision(clientToken)

            // Then
            assertEquals("transaction123", result.transactionNumber)
            assertEquals("http://status.url", result.statusUrl)
            assertEquals("http://complete.url", result.completeUrl)
        }

    @Test
    fun `supportedClientTokenIntents should return correct list of intents`() {
        // Given
        every { tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType } returns "NOL_PAY"

        // When
        val result = nolPayResumeHandler.supportedClientTokenIntents()

        // Then
        assertEquals(listOf("NOL_PAY_REDIRECTION"), result)
    }

    @Test
    fun `supportedClientTokenIntents should return empty list when paymentMethodType is null`() {
        // Given
        every { tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType } returns null

        // When
        val result = nolPayResumeHandler.supportedClientTokenIntents()

        // Then
        assertEquals("_REDIRECTION", result.first())
    }
}

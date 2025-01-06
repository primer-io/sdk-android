package io.primer.android.stripe.ach.implementation.payment.resume.handler

import io.mockk.every
import io.mockk.mockk
import io.primer.android.clientToken.core.token.domain.repository.ClientTokenRepository
import io.primer.android.clientToken.core.validation.domain.repository.ValidateClientTokenRepository
import io.primer.android.payments.core.helpers.CheckoutAdditionalInfoHandler
import io.primer.android.payments.core.tokenization.domain.repository.TokenizedPaymentMethodRepository
import io.primer.android.stripe.ach.implementation.payment.resume.clientToken.data.StripeAchPaymentMethodClientTokenParser
import io.primer.android.stripe.ach.implementation.payment.resume.clientToken.domain.model.StripeAchClientToken
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class StripeAchVaultResumeDecisionHandlerTest {
    private lateinit var clientTokenParser: StripeAchPaymentMethodClientTokenParser
    private lateinit var validateClientTokenRepository: ValidateClientTokenRepository
    private lateinit var clientTokenRepository: ClientTokenRepository
    private lateinit var tokenizedPaymentMethodRepository: TokenizedPaymentMethodRepository
    private lateinit var checkoutAdditionalInfoHandler: CheckoutAdditionalInfoHandler
    private lateinit var resumeHandler: StripeAchVaultResumeDecisionHandler

    @BeforeEach
    fun setUp() {
        clientTokenParser = mockk()
        validateClientTokenRepository = mockk()
        clientTokenRepository = mockk()
        tokenizedPaymentMethodRepository = mockk()
        checkoutAdditionalInfoHandler = mockk()

        resumeHandler =
            StripeAchVaultResumeDecisionHandler(
                clientTokenParser,
                tokenizedPaymentMethodRepository,
                validateClientTokenRepository,
                clientTokenRepository,
                checkoutAdditionalInfoHandler,
            )
    }

    @Test
    fun `getResumeDecision should return correct StripeAchVaultResumeDecision`() =
        runTest {
            // Given
            val clientToken =
                StripeAchClientToken(
                    clientTokenIntent = "intent",
                    sdkCompleteUrl = "sdkCompleteUrl",
                    stripePaymentIntentId = "stripePaymentIntentId",
                    stripeClientSecret = "stripeClientSecret",
                )

            // When
            val result = resumeHandler.getResumeDecision(clientToken)

            // Then
            assertEquals(
                StripeAchVaultDecision(
                    sdkCompleteUrl = "sdkCompleteUrl",
                ),
                result,
            )
        }

    @Test
    fun `supportedClientTokenIntents should return correct list of intents`() {
        // Given
        every { tokenizedPaymentMethodRepository.getPaymentMethod().paymentMethodType } returns "STRIPE_ACH"

        // When
        val result = resumeHandler.supportedClientTokenIntents()

        // Then
        assertEquals(listOf("STRIPE_ACH_REDIRECTION"), result)
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

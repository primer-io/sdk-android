package io.primer.android.stripe.ach.implementation.payment.resume.clientToken.data

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verify
import io.primer.android.stripe.ach.implementation.payment.resume.clientToken.data.model.StripeAchClientTokenData
import io.primer.android.stripe.ach.implementation.payment.resume.clientToken.domain.model.StripeAchClientToken
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class StripeAchPaymentMethodClientTokenParserTest {
    @InjectMockKs
    private lateinit var parser: StripeAchPaymentMethodClientTokenParser

    @Test
    fun `parseClientToken() correctly parses client token`() {
        val clientToken = "token"
        val clientTokenData = StripeAchClientTokenData(
            sdkCompleteUrl = "https://complete.url",
            stripePaymentIntentId = "id",
            stripeClientSecret = "secret",
            intent = "intent"
        )
        mockkObject(StripeAchClientTokenData.Companion)
        every { StripeAchClientTokenData.fromString(clientToken) } returns clientTokenData

        val result = parser.parseClientToken(clientToken)

        val expectedToken = StripeAchClientToken(
            sdkCompleteUrl = "https://complete.url",
            stripePaymentIntentId = "id",
            stripeClientSecret = "secret",
            clientTokenIntent = "intent"
        )
        assertEquals(expectedToken, result)
        verify { StripeAchClientTokenData.fromString(clientToken) }
        unmockkObject(StripeAchClientTokenData.Companion)
    }
}

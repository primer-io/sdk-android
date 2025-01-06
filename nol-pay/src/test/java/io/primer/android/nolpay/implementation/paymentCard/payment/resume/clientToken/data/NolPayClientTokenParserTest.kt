package io.primer.android.nolpay.implementation.paymentCard.payment.resume.clientToken.data

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.primer.android.clientToken.core.validation.data.utils.ClientTokenDecoder
import io.primer.android.nolpay.implementation.paymentCard.payment.resume.clientToken.data.model.NolPayClientTokenData
import io.primer.android.nolpay.implementation.paymentCard.payment.resume.clientToken.domain.model.NolPayClientToken
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

internal class NolPayClientTokenParserTest {
    @BeforeEach
    fun setUp() {
        mockkObject(NolPayClientTokenData.Companion)
        mockkObject(ClientTokenDecoder)
        mockkStatic(android.util.Base64::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `parseClientToken should correctly parse a valid client token string`() {
        // Given
        val expectedClientTokenData =
            NolPayClientTokenData(
                intent = "intent",
                transactionNumber = "transactionNumber",
                statusUrl = "https://example.com/status",
                completeUrl = "https://example.com/complete",
            )
        val expectedClientToken =
            NolPayClientToken(
                clientTokenIntent = expectedClientTokenData.intent,
                transactionNumber = expectedClientTokenData.transactionNumber,
                statusUrl = expectedClientTokenData.statusUrl,
                completeUrl = expectedClientTokenData.completeUrl,
            )

        val token = "eyJhY2Nlc3NUb2tlbiI6ICJ5b3VyX3Rva2VuX2hlcmUifQ=="

        every { android.util.Base64.decode(any<String>(), android.util.Base64.URL_SAFE) } returns token.toByteArray()

        every { NolPayClientTokenData.fromString(token) } returns expectedClientTokenData

        // When
        val parser = NolPayClientTokenParser()
        val result = parser.parseClientToken(token)

        // Then
        assertEquals(expectedClientToken, result)
    }

    @Test
    fun `parseClientToken should throw exception for invalid client token string`() {
        // Given
        val clientToken = "YWNjZXNzVG9rZW4="

        every { ClientTokenDecoder.decode(any()) } returns clientToken
        every { NolPayClientTokenData.fromString(clientToken) } throws IllegalArgumentException("Invalid client token")

        val parser = NolPayClientTokenParser()

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            parser.parseClientToken(clientToken)
        }
    }
}

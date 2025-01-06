package io.primer.android.banks.implementation.payment.resume.clientToken.data.model

import io.mockk.every
import io.mockk.mockkObject
import io.primer.android.clientToken.core.errors.data.exception.ExpiredClientTokenException
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import io.primer.android.clientToken.core.validation.data.utils.ClientTokenDecoder
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BankIssuerClientTokenDataTest {
    private val encodedToken = "encodedToken"
    private val decodedToken =
        """
        {
            "intent": "BANK_ISSUER",
            "statusUrl": "http://status.url",
            "redirectUrl": "http://redirect.url"
        }
        """.trimIndent()

    @BeforeEach
    fun setUp() {
        // Mock the static methods in ClientTokenDecoder and JSONSerializationUtils
        mockkObject(ClientTokenDecoder)
        mockkObject(JSONSerializationUtils)
    }

    @Test
    fun `fromString should deserialize correctly when the token is correct`() {
        // Arrange
        every { ClientTokenDecoder.decode(encodedToken) } returns decodedToken

        // Act
        val result = BankIssuerClientTokenData.fromString(encodedToken)

        // Assert
        assertEquals("BANK_ISSUER", result.intent)
        assertEquals("http://status.url", result.statusUrl)
        assertEquals("http://redirect.url", result.redirectUrl)
    }

    @Test
    fun `fromString should throw InvalidClientTokenException when invalid token`() {
        // Arrange
        every { ClientTokenDecoder.decode(encodedToken) } throws InvalidClientTokenException()

        // Act & Assert
        assertThrows<InvalidClientTokenException> {
            BankIssuerClientTokenData.fromString(encodedToken)
        }
    }

    @Test
    fun `fromString should throw ExpiredClientTokenException when expired token`() {
        // Arrange
        every { ClientTokenDecoder.decode(encodedToken) } throws ExpiredClientTokenException()

        // Act & Assert
        assertThrows<ExpiredClientTokenException> {
            BankIssuerClientTokenData.fromString(encodedToken)
        }
    }
}

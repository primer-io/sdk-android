package io.primer.android.qrcode.implementation.payment.resume.clientToken.data.model

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

class QrCodeClientTokenDataTest {
    private val encodedToken = "encodedToken"
    private val decodedToken =
        """
        {
            "intent": "intent",
            "expiresAt": "expiresAt",
            "qrCodeUrl": "qrCodeUrl",
            "qrCode": "qrCode",
            "statusUrl": "statusUrl"
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
        val result = QrCodeClientTokenData.fromString(encodedToken)

        // Assert
        assertEquals("intent", result.intent)
        assertEquals("expiresAt", result.expiresAt)
        assertEquals("qrCodeUrl", result.qrCodeUrl)
        assertEquals("qrCode", result.qrCodeBase64)
        assertEquals("statusUrl", result.statusUrl)
    }

    @Test
    fun `fromString should throw InvalidClientTokenException when invalid token`() {
        // Arrange
        every { ClientTokenDecoder.decode(encodedToken) } throws InvalidClientTokenException()

        // Act & Assert
        assertThrows<InvalidClientTokenException> {
            QrCodeClientTokenData.fromString(encodedToken)
        }
    }

    @Test
    fun `fromString should throw ExpiredClientTokenException when expired token`() {
        // Arrange
        every { ClientTokenDecoder.decode(encodedToken) } throws ExpiredClientTokenException()

        // Act & Assert
        assertThrows<ExpiredClientTokenException> {
            QrCodeClientTokenData.fromString(encodedToken)
        }
    }
}

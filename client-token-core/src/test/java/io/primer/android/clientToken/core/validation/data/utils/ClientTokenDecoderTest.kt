package io.primer.android.clientToken.core.validation.data.utils

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ClientTokenDecoderTest {
    @BeforeEach
    fun setUp() {
        mockkStatic(android.util.Base64::class)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `decode should return decoded token when valid encoded token is provided`() {
        // Arrange
        val validToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhY2Nlc3NUb2tlbiI6InRlc3RUb2tlbiJ9.abc123"
        val expectedDecoded = "{\"accessToken\":\"testToken\"}"

        every {
            android.util.Base64.decode(any<String>(), android.util.Base64.URL_SAFE)
        } returns expectedDecoded.toByteArray()

        // Act
        val result = ClientTokenDecoder.decode(validToken)

        // Assert
        assertEquals(expectedDecoded, result)
    }

    @Test
    fun `decode should throw InvalidClientTokenException when token is blank`() {
        // Arrange
        val blankToken = ""

        // Act & Assert
        assertThrows<InvalidClientTokenException> {
            ClientTokenDecoder.decode(blankToken)
        }
    }

    @Test
    fun `decode should throw InvalidClientTokenException when token is invalid`() {
        // Act & Assert
        assertThrows<InvalidClientTokenException> {
            ClientTokenDecoder.decode("")
        }
    }

    @Test
    fun `decode should throw InvalidClientTokenException when token doesn't contain accessToken`() {
        // Arrange
        val tokenWithoutAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHBpcmVkIjoiZXhwaXJlZFRva2VuIn0.abc123"
        val expectedDecoded = "{}"

        every {
            android.util.Base64.decode(
                any<String>(),
                android.util.Base64.URL_SAFE,
            )
        } returns expectedDecoded.toByteArray()

        // Act & Assert
        assertThrows<InvalidClientTokenException> {
            ClientTokenDecoder.decode(tokenWithoutAccessToken)
        }
    }
}

package io.primer.android.googlepay.implementation.payment.resume.clientToken.data.model

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.primer.android.clientToken.core.errors.data.exception.ExpiredClientTokenException
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import io.primer.android.clientToken.core.validation.data.utils.ClientTokenDecoder
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class GooglePayProcessor3DSClientTokenDataTest {

    private val validEncodedString = "validEncodedString"
    private val decodedString = """
        {
            "intent": "testIntent",
            "statusUrl": "https://www.example.com/status",
            "redirectUrl": "https://www.example.com/redirect",
        }
    """

    @BeforeEach
    fun setUp() {
        mockkObject(ClientTokenDecoder)
        mockkStatic(JSONSerializationUtils::class)
    }

    @Test
    fun `fromString should correctly decode and deserialize when valid data`() {
        // Given
        every { ClientTokenDecoder.decode(validEncodedString) } returns decodedString

        // When
        val result = GooglePayProcessor3DSClientTokenData.fromString(validEncodedString)

        // Then
        val expected = GooglePayProcessor3DSClientTokenData(
            intent = "testIntent",
            statusUrl = "https://www.example.com/status",
            redirectUrl = "https://www.example.com/redirect"
        )
        assertEquals(expected, result)
    }

    @Test
    fun `fromString should throw InvalidClientTokenException when invalid client token`() {
        // Define an invalid client token
        val emptyClientToken = ""

        // Expect an InvalidClientTokenException to be thrown when attempting to parse the invalid client token
        assertThrows(InvalidClientTokenException::class.java) {
            GooglePayProcessor3DSClientTokenData.fromString(emptyClientToken)
        }
    }

    @Test
    fun `fromString should throw ExpiredClientTokenException when token is expired`() {
        // Given
        every { ClientTokenDecoder.decode(any()) } throws ExpiredClientTokenException()

        // When / Then
        assertFailsWith<ExpiredClientTokenException> {
            GooglePayProcessor3DSClientTokenData.fromString(validEncodedString)
        }
    }
}

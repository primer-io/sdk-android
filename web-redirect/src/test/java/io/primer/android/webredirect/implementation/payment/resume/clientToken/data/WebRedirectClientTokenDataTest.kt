package io.primer.android.webredirect.implementation.payment.resume.clientToken.data

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.primer.android.clientToken.core.errors.data.exception.ExpiredClientTokenException
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import io.primer.android.clientToken.core.validation.data.utils.ClientTokenDecoder
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.webredirect.implementation.payment.resume.clientToken.data.model.WebRedirectClientTokenData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class WebRedirectClientTokenDataTest {
    private val validEncodedString = "validEncodedString"
    private val decodedString = """
        {
            "intent": "testIntent",
            "statusUrl": "testStatusUrl",
            "redirectUrl": "testRedirectUrl"
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
        val result = WebRedirectClientTokenData.fromString(validEncodedString)

        // Then
        val expected =
            WebRedirectClientTokenData(
                intent = "testIntent",
                statusUrl = "testStatusUrl",
                redirectUrl = "testRedirectUrl",
            )
        assertEquals(expected, result)
    }

    @Test
    fun `fromString should throw InvalidClientTokenException when invalid client token`() {
        // Define an invalid client token
        val emptyClientToken = ""

        // Expect an InvalidClientTokenException to be thrown when attempting to parse the invalid client token
        assertThrows(InvalidClientTokenException::class.java) {
            WebRedirectClientTokenData.fromString(emptyClientToken)
        }
    }

    @Test
    fun `fromString should throw ExpiredClientTokenException when token is expired`() {
        // Given
        every { ClientTokenDecoder.decode(any()) } throws ExpiredClientTokenException()

        // When / Then
        assertFailsWith<ExpiredClientTokenException> {
            WebRedirectClientTokenData.fromString(validEncodedString)
        }
    }
}

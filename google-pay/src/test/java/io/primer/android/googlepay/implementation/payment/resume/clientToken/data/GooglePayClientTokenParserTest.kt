package io.primer.android.googlepay.implementation.payment.resume.clientToken.data

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.primer.android.googlepay.implementation.payment.resume.clientToken.data.model.GooglePayNative3DSClientTokenData
import io.primer.android.googlepay.implementation.payment.resume.clientToken.data.model.GooglePayProcessor3DSClientTokenData
import io.primer.android.googlepay.implementation.payment.resume.clientToken.domain.model.GooglePayClientToken
import io.primer.android.processor3ds.domain.model.Processor3DS
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class GooglePayClientTokenParserTest {

    private val validEncodedString = "validEncodedString"

    @BeforeEach
    fun setUp() {
        mockkObject(GooglePayNative3DSClientTokenData)
        mockkObject(GooglePayProcessor3DSClientTokenData)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `parseClientToken should correctly parse when called with a valid Native 3DS client token`() {
        // Given
        every { GooglePayNative3DSClientTokenData.fromString(validEncodedString) } returns
            GooglePayNative3DSClientTokenData("testIntent", listOf("1.0", "2.0"))

        val parser = GooglePayClientTokenParser()

        // When
        val result = parser.parseClientToken(validEncodedString)

        // Then
        val expected = GooglePayClientToken.GooglePayNative3DSClientToken(
            "testIntent",
            listOf("1.0", "2.0")
        )
        assertEquals(expected, result)
    }

    @Test
    fun `parseClientToken should correctly parse when called with a valid Processor 3DS client token`() {
        // Given
        every { GooglePayProcessor3DSClientTokenData.fromString(validEncodedString) } returns
            GooglePayProcessor3DSClientTokenData(
                intent = "testIntent",
                statusUrl = "https://www.example.com/status",
                redirectUrl = "https://www.example.com/redirect"
            )

        val parser = GooglePayClientTokenParser()

        // When
        val result = parser.parseClientToken(validEncodedString)

        // Then
        val expected = GooglePayClientToken.GooglePayProcessor3DSClientToken(
            clientTokenIntent = "testIntent",
            processor3DS = Processor3DS(
                statusUrl = "https://www.example.com/status",
                redirectUrl = "https://www.example.com/redirect"
            )
        )
        assertEquals(expected, result)
    }

    @Test
    fun `parseClientToken should throw exception when called with an invalid client token`() {
        // Given
        every { GooglePayNative3DSClientTokenData.fromString(validEncodedString) } throws IllegalArgumentException()
        every { GooglePayProcessor3DSClientTokenData.fromString(validEncodedString) } throws IllegalArgumentException()

        val parser = GooglePayClientTokenParser()

        // When / Then
        assertFailsWith<IllegalArgumentException> {
            parser.parseClientToken(validEncodedString)
        }
    }
}

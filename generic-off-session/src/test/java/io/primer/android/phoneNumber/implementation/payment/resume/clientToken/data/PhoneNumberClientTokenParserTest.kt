package io.primer.android.phoneNumber.implementation.payment.resume.clientToken.data

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.primer.android.phoneNumber.implementation.payment.resume.clientToken.data.model.PhoneNumberClientTokenData
import io.primer.android.phoneNumber.implementation.payment.resume.domain.model.PhoneNumberClientToken
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class PhoneNumberClientTokenParserTest {
    private val validEncodedString = "validEncodedString"

    @BeforeEach
    fun setUp() {
        mockkObject(PhoneNumberClientTokenData)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `parseClientToken should correctly parse when called with a valid client token`() {
        // Given
        val intent = "testIntent"
        val statusUrl = "statusUrl"
        every { PhoneNumberClientTokenData.fromString(validEncodedString) } returns
            PhoneNumberClientTokenData(intent, statusUrl)

        val parser = PhoneNumberClientTokenParser()

        // When
        val result = parser.parseClientToken(validEncodedString)

        // Then
        val expected = PhoneNumberClientToken(statusUrl, intent)
        assertEquals(expected, result)
    }

    @Test
    fun `parseClientToken should throw exception when called with an invalid client token`() {
        // Given
        every { PhoneNumberClientTokenData.fromString(validEncodedString) } throws IllegalArgumentException()

        val parser = PhoneNumberClientTokenParser()

        // When / Then
        assertFailsWith<IllegalArgumentException> {
            parser.parseClientToken(validEncodedString)
        }
    }
}

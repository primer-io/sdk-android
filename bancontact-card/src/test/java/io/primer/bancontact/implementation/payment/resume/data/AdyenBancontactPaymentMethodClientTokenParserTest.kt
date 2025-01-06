package io.primer.bancontact.implementation.payment.resume.data

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.primer.android.bancontact.implementation.payment.resume.clientToken.data.AdyenBancontactPaymentMethodClientTokenParser
import io.primer.android.bancontact.implementation.payment.resume.clientToken.data.model.AdyenBancontactClientTokenData
import io.primer.android.bancontact.implementation.payment.resume.clientToken.domain.model.AdyenBancontactClientToken
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class AdyenBancontactPaymentMethodClientTokenParserTest {
    private val validEncodedString = "validEncodedString"

    @BeforeEach
    fun setUp() {
        mockkObject(AdyenBancontactClientTokenData)
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
        val redirectUrl = "redirectUrl"
        every { AdyenBancontactClientTokenData.fromString(validEncodedString) } returns
            AdyenBancontactClientTokenData(intent, statusUrl, redirectUrl)

        val parser = AdyenBancontactPaymentMethodClientTokenParser()

        // When
        val result = parser.parseClientToken(validEncodedString)

        // Then
        val expected = AdyenBancontactClientToken(redirectUrl, statusUrl, intent)
        assertEquals(expected, result)
    }

    @Test
    fun `parseClientToken should throw exception when called with an invalid client token`() {
        // Given
        every { AdyenBancontactClientTokenData.fromString(validEncodedString) } throws IllegalArgumentException()

        val parser = AdyenBancontactPaymentMethodClientTokenParser()

        // When / Then
        assertFailsWith<IllegalArgumentException> {
            parser.parseClientToken(validEncodedString)
        }
    }
}

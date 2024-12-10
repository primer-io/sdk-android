package io.primer.android.webredirect.implementation.payment.resume.clientToken.data

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.primer.android.webredirect.implementation.payment.resume.clientToken.data.model.WebRedirectClientTokenData
import io.primer.android.webredirect.implementation.payment.resume.clientToken.domain.model.WebRedirectClientToken
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class WebRedirectPaymentMethodClientTokenParserTest {

    private val validEncodedString = "validEncodedString"

    @BeforeEach
    fun setUp() {
        mockkObject(WebRedirectClientTokenData)
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
        every { WebRedirectClientTokenData.fromString(validEncodedString) } returns
            WebRedirectClientTokenData(intent, statusUrl, redirectUrl)

        val parser = WebRedirectPaymentMethodClientTokenParser()

        // When
        val result = parser.parseClientToken(validEncodedString)

        // Then
        val expected = WebRedirectClientToken(redirectUrl, statusUrl, intent)
        assertEquals(expected, result)
    }

    @Test
    fun `parseClientToken should throw exception when called with an invalid client token`() {
        // Given
        every { WebRedirectClientTokenData.fromString(validEncodedString) } throws IllegalArgumentException()

        val parser = WebRedirectPaymentMethodClientTokenParser()

        // When / Then
        assertFailsWith<IllegalArgumentException> {
            parser.parseClientToken(validEncodedString)
        }
    }
}

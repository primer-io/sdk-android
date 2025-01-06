package io.primer.android.ipay88.implementation.payment.resume.clientToken.data

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.primer.android.ipay88.implementation.payment.resume.clientToken.data.model.IPay88ClientTokenData
import io.primer.android.ipay88.implementation.payment.resume.clientToken.domain.model.IPay88ClientToken
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class IPay88ClientTokenParserTest {
    private val parser = IPay88ClientTokenParser()

    @BeforeEach
    fun setUp() {
        mockkObject(IPay88ClientTokenData)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `parseClientToken should correctly parse client token`() {
        // Given
        val clientToken = "encodedClientToken"

        val expectedToken =
            IPay88ClientToken(
                intent = "PAY",
                statusUrl = "http://example.com/status",
                paymentId = "payment123",
                paymentMethod = 0,
                actionType = "actionType",
                referenceNumber = "ref123",
                supportedCurrencyCode = "MYR",
                backendCallbackUrl = "http://example.com/callback",
                supportedCountryCode = "MY",
                clientTokenIntent = "PAY",
            )

        every { IPay88ClientTokenData.fromString(clientToken) } returns
            IPay88ClientTokenData(
                intent = "PAY",
                statusUrl = "http://example.com/status",
                paymentId = "payment123",
                paymentMethod = 0,
                actionType = "actionType",
                referenceNumber = "ref123",
                currencyCode = "MYR",
                countryCode = "MY",
                backendCallbackUrl = "http://example.com/callback",
            )

        // When
        val result = parser.parseClientToken(clientToken)

        // Then
        assertEquals(result, expectedToken)
    }
}

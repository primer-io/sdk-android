package io.primer.android.banks.implementation.payment.resume.clientToken.data

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.primer.android.banks.implementation.payment.resume.clientToken.domain.model.BankIssuerClientToken
import io.primer.android.banks.implementation.payment.resume.clientToken.data.model.BankIssuerClientTokenData
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class BankIssuerPaymentMethodClientTokenParserTest {

    private val validEncodedString = "validEncodedString"

    @BeforeEach
    fun setUp() {
        mockkObject(BankIssuerClientTokenData)
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
        every { BankIssuerClientTokenData.fromString(validEncodedString) } returns
            BankIssuerClientTokenData(intent, statusUrl, redirectUrl)

        val parser = BankIssuerPaymentMethodClientTokenParser()

        // When
        val result = parser.parseClientToken(validEncodedString)

        // Then
        val expected = BankIssuerClientToken(redirectUrl, statusUrl, intent)
        assertEquals(expected, result)
    }

    @Test
    fun `parseClientToken should throw exception when called with an invalid client token`() {
        // Given
        every { BankIssuerClientTokenData.fromString(validEncodedString) } throws IllegalArgumentException()

        val parser = BankIssuerPaymentMethodClientTokenParser()

        // When / Then
        assertFailsWith<IllegalArgumentException> {
            parser.parseClientToken(validEncodedString)
        }
    }
}

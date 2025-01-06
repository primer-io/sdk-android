package io.primer.android.vouchers.retailOutlets.implementation.payment.resume.clientToken.data

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.primer.android.vouchers.retailOutlets.implementation.payment.resume.clientToken.data.model.RetailOutletsClientTokenData
import io.primer.android.vouchers.retailOutlets.implementation.payment.resume.clientToken.domain.model.RetailOutletsClientToken
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class RetailOutletsClientTokenParserTest {
    private val validEncodedString = "validEncodedString"

    @BeforeEach
    fun setUp() {
        mockkObject(RetailOutletsClientTokenData)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `parseClientToken should correctly parse when called with a valid client token`() {
        // Given
        val intent = "testIntent"
        val expiresAt = "expiresAt"
        val reference = "reference"
        val entity = "entity"
        every { RetailOutletsClientTokenData.fromString(validEncodedString) } returns
            RetailOutletsClientTokenData(intent, expiresAt, reference, entity)

        val parser = RetailOutletsClientTokenParser()

        // When
        val result = parser.parseClientToken(validEncodedString)

        // Then
        val expected = RetailOutletsClientToken(intent, expiresAt, reference, entity)
        assertEquals(expected, result)
    }

    @Test
    fun `parseClientToken should throw exception when called with an invalid client token`() {
        // Given
        every { RetailOutletsClientTokenData.fromString(validEncodedString) } throws IllegalArgumentException()

        val parser = RetailOutletsClientTokenParser()

        // When / Then
        assertFailsWith<IllegalArgumentException> {
            parser.parseClientToken(validEncodedString)
        }
    }
}

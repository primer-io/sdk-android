package io.primer.android.vouchers.multibanco.implementation.payment.resume.clientToken.data

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.primer.android.vouchers.multibanco.implementation.payment.resume.clientToken.data.model.MultibancoClientTokenData
import io.primer.android.vouchers.multibanco.implementation.payment.resume.clientToken.domain.model.MultibancoClientToken
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class MultibancoClientTokenParserTest {
    private val validEncodedString = "validEncodedString"

    @BeforeEach
    fun setUp() {
        mockkObject(MultibancoClientTokenData)
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
        every { MultibancoClientTokenData.fromString(validEncodedString) } returns
            MultibancoClientTokenData(intent, expiresAt, reference, entity)

        val parser = MultibancoClientTokenParser()

        // When
        val result = parser.parseClientToken(validEncodedString)

        // Then
        val expected = MultibancoClientToken(intent, expiresAt, reference, entity)
        assertEquals(expected, result)
    }

    @Test
    fun `parseClientToken should throw exception when called with an invalid client token`() {
        // Given
        every { MultibancoClientTokenData.fromString(validEncodedString) } throws IllegalArgumentException()

        val parser = MultibancoClientTokenParser()

        // When / Then
        assertFailsWith<IllegalArgumentException> {
            parser.parseClientToken(validEncodedString)
        }
    }
}

package io.primer.android.qrcode.implementation.payment.resume.clientToken.data

import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.primer.android.qrcode.implementation.payment.resume.clientToken.data.model.QrCodeClientTokenData
import io.primer.android.qrcode.implementation.payment.resume.domain.model.QrCodeClientToken
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class QrCodeClientTokenParserTest {
    private val validEncodedString = "validEncodedString"

    @BeforeEach
    fun setUp() {
        mockkObject(QrCodeClientTokenData)
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
        val expiresAt = "expiresAt"
        val qrCodeUrl = "qrCodeUrl"
        val qrCodeBase64 = "qrCode"
        every { QrCodeClientTokenData.fromString(validEncodedString) } returns
            QrCodeClientTokenData(intent, statusUrl, expiresAt, qrCodeUrl, qrCodeBase64)

        val parser = QrCodeClientTokenParser()

        // When
        val result = parser.parseClientToken(validEncodedString)

        // Then
        val expected = QrCodeClientToken(intent, statusUrl, expiresAt, qrCodeUrl, qrCodeBase64)
        assertEquals(expected, result)
    }

    @Test
    fun `parseClientToken should throw exception when called with an invalid client token`() {
        // Given
        every { QrCodeClientTokenData.fromString(validEncodedString) } throws IllegalArgumentException()

        val parser = QrCodeClientTokenParser()

        // When / Then
        assertFailsWith<IllegalArgumentException> {
            parser.parseClientToken(validEncodedString)
        }
    }
}

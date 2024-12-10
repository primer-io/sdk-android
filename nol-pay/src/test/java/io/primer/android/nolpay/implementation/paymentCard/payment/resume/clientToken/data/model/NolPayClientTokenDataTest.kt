package io.primer.android.nolpay.implementation.paymentCard.payment.resume.clientToken.data.model

import io.mockk.every
import io.mockk.junit5.MockKExtension
import io.mockk.mockkObject
import io.primer.android.clientToken.core.errors.data.exception.ExpiredClientTokenException
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import io.primer.android.clientToken.core.validation.data.utils.ClientTokenDecoder
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class NolPayClientTokenDataTest {

    @BeforeEach
    fun setUp() {
        mockkObject(ClientTokenDecoder)
        mockkObject(JSONSerializationUtils)
    }

    @Test
    fun `fromString should parse valid encoded string`() {
        // Given
        val encodedString = "validEncodedString"
        val decodedString = """{
            "intent": "testIntent",
            "nolPayTransactionNo": "testTransactionNumber",
            "statusUrl": "testStatusUrl",
            "redirectUrl": "testCompleteUrl"
        }"""
        val expectedTokenData = NolPayClientTokenData(
            intent = "testIntent",
            transactionNumber = "testTransactionNumber",
            statusUrl = "testStatusUrl",
            completeUrl = "testCompleteUrl"
        )

        every { ClientTokenDecoder.decode(encodedString) } returns decodedString

        // When
        val result = NolPayClientTokenData.fromString(encodedString)

        // Then
        assertEquals(expectedTokenData, result)
    }

    @Test
    fun `fromString should throw InvalidClientTokenException for invalid encoded string`() {
        // Given
        val invalidEncodedString = ""

        every { ClientTokenDecoder.decode(invalidEncodedString) } throws InvalidClientTokenException()

        // Then
        assertThrows(InvalidClientTokenException::class.java) {
            // When
            NolPayClientTokenData.fromString(invalidEncodedString)
        }
    }

    @Test
    fun `fromString should throw ExpiredClientTokenException for expired client token`() {
        // Given
        val expiredEncodedString = "expiredEncodedString"

        every { ClientTokenDecoder.decode(expiredEncodedString) } throws ExpiredClientTokenException()

        // Then
        assertThrows(ExpiredClientTokenException::class.java) {
            // When
            NolPayClientTokenData.fromString(expiredEncodedString)
        }
    }
}

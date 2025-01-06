package io.primer.android.ipay88.implementation.payment.resume.clientToken.data.model

import com.ipay.IPayIH
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.mockkStatic
import io.primer.android.clientToken.core.errors.data.exception.ExpiredClientTokenException
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import io.primer.android.clientToken.core.validation.data.utils.ClientTokenDecoder
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertFailsWith

class IPay88ClientTokenDataTest {
    private val validEncodedString = "validEncodedString"
    private val decodedString = """
        {
            "intent": "testIntent",
            "statusUrl": "testStatusUrl",
            "iPay88PaymentMethodId": "iPay88PaymentMethodId",
            "iPay88ActionType": "iPay88ActionType",
            "primerTransactionId": "primerTransactionId",
            "supportedCountry": "supportedCountry",
            "supportedCurrencyCode": "supportedCurrencyCode",
            "backendCallbackUrl": "backendCallbackUrl"
        }
    """

    @BeforeEach
    fun setUp() {
        mockkObject(ClientTokenDecoder)
        mockkStatic(JSONSerializationUtils::class)
    }

    @Test
    fun `fromString should correctly decode and deserialize when valid data`() {
        // Given
        every { ClientTokenDecoder.decode(validEncodedString) } returns decodedString

        // When
        val result = IPay88ClientTokenData.fromString(validEncodedString)

        // Then
        val expected =
            IPay88ClientTokenData(
                intent = "testIntent",
                statusUrl = "testStatusUrl",
                paymentId = "iPay88PaymentMethodId",
                paymentMethod = IPayIH.PAY_METHOD_CREDIT_CARD,
                actionType = "iPay88ActionType",
                referenceNumber = "primerTransactionId",
                countryCode = "supportedCountry",
                currencyCode = "supportedCurrencyCode",
                backendCallbackUrl = "backendCallbackUrl",
            )
        assertEquals(expected, result)
    }

    @Test
    fun `fromString should throw InvalidClientTokenException when invalid client token`() {
        // Define an invalid client token
        val emptyClientToken = ""

        // Expect an InvalidClientTokenException to be thrown when attempting to parse the invalid client token
        assertThrows(InvalidClientTokenException::class.java) {
            IPay88ClientTokenData.fromString(emptyClientToken)
        }
    }

    @Test
    fun `fromString should throw ExpiredClientTokenException when token is expired`() {
        // Given
        every { ClientTokenDecoder.decode(any()) } throws ExpiredClientTokenException()

        // When / Then
        assertFailsWith<ExpiredClientTokenException> {
            IPay88ClientTokenData.fromString(validEncodedString)
        }
    }
}

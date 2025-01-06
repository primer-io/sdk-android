package io.primer.android.card.implementation.tokenization.data.model

import io.primer.android.configuration.data.model.CardNetwork
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CardPaymentInstrumentDataRequestTest {
    private lateinit var request: CardPaymentInstrumentDataRequest

    @BeforeEach
    fun setUp() {
        request =
            CardPaymentInstrumentDataRequest(
                number = "4111111111111111",
                expirationMonth = "12",
                expirationYear = "2024",
                cvv = "123",
                cardholderName = "John Doe",
                preferredNetwork = CardNetwork.Type.VISA,
            )
    }

    @Test
    fun `serializer should correctly serialize CardPaymentInstrumentDataRequest to JSONObject`() {
        // Arrange
        val expectedJson =
            JSONObject().apply {
                put("number", "4111111111111111")
                put("expirationMonth", "12")
                put("expirationYear", "2024")
                put("cvv", "123")
                putOpt("cardholderName", "John Doe")
                putOpt("preferredNetwork", "VISA")
            }

        // Act
        val actualJson = CardPaymentInstrumentDataRequest.serializer.serialize(request)

        // Assert
        assertEquals(expectedJson.toString(), actualJson.toString())
    }

    @Test
    fun `serializer should handle null cardholderName and preferredNetwork`() {
        // Arrange
        val requestWithNulls =
            CardPaymentInstrumentDataRequest(
                number = "4111111111111111",
                expirationMonth = "12",
                expirationYear = "2024",
                cvv = "123",
                cardholderName = null,
                preferredNetwork = null,
            )
        val expectedJson =
            JSONObject().apply {
                put("number", "4111111111111111")
                put("expirationMonth", "12")
                put("expirationYear", "2024")
                put("cvv", "123")
                putOpt("cardholderName", null)
                putOpt("preferredNetwork", null)
            }

        // Act
        val actualJson = CardPaymentInstrumentDataRequest.serializer.serialize(requestWithNulls)

        // Assert
        assertEquals(expectedJson.toString(), actualJson.toString())
    }

    @Test
    fun `serializer should handle empty strings`() {
        // Arrange
        val requestWithEmptyFields =
            CardPaymentInstrumentDataRequest(
                number = "",
                expirationMonth = "",
                expirationYear = "",
                cvv = "",
                cardholderName = "",
                preferredNetwork = CardNetwork.Type.VISA,
            )
        val expectedJson =
            JSONObject().apply {
                put("number", "")
                put("expirationMonth", "")
                put("expirationYear", "")
                put("cvv", "")
                putOpt("cardholderName", "")
                putOpt("preferredNetwork", "VISA")
            }

        // Act
        val actualJson = CardPaymentInstrumentDataRequest.serializer.serialize(requestWithEmptyFields)

        // Assert
        assertEquals(expectedJson.toString(), actualJson.toString())
    }
}

package io.primer.android.paypal.implementation.tokenization.data.model

import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrderInfo
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaypalOrderInfoResponseTest {
    @Test
    fun `PaypalOrderInfoResponse should deserialize correctly`() {
        // Arrange
        val json =
            JSONObject().apply {
                put("orderId", "order123")
                put(
                    "externalPayerInfo",
                    JSONObject()
                        .apply {
                            put("externalPayerId", "payer123")
                            put("email", "email@example.com")
                            put("firstName", "John")
                            put("lastName", "Doe")
                        },
                )
            }

        // Act
        val response = PaypalOrderInfoResponse.deserializer.deserialize(json)

        // Assert
        val expectedResponse =
            PaypalOrderInfoResponse(
                orderId = "order123",
                externalPayerInfo =
                    PaypalExternalPayerInfo(
                        externalPayerId = "payer123",
                        email = "email@example.com",
                        firstName = "John",
                        lastName = "Doe",
                    ),
            )
        assertEquals(expectedResponse, response)
    }

    @Test
    fun `PaypalExternalPayerInfo should deserialize correctly`() {
        // Arrange
        val json =
            JSONObject().apply {
                put("externalPayerId", "payer123")
                put("email", "email@example.com")
                put("firstName", "John")
                put("lastName", "Doe")
            }

        // Act
        val externalPayerInfo = PaypalExternalPayerInfo.deserializer.deserialize(json)

        // Assert
        val expectedExternalPayerInfo =
            PaypalExternalPayerInfo(
                externalPayerId = "payer123",
                email = "email@example.com",
                firstName = "John",
                lastName = "Doe",
            )
        assertEquals(expectedExternalPayerInfo, externalPayerInfo)
    }

    @Test
    fun `PaypalExternalPayerInfo should serialize correctly`() {
        // Arrange
        val externalPayerInfo =
            PaypalExternalPayerInfo(
                externalPayerId = "payer123",
                email = "email@example.com",
                firstName = "John",
                lastName = "Doe",
            )

        // Act
        val json = PaypalExternalPayerInfo.serializer.serialize(externalPayerInfo)

        // Assert
        val expectedJson =
            JSONObject().apply {
                put("externalPayerId", "payer123")
                put("email", "email@example.com")
                put("firstName", "John")
                put("lastName", "Doe")
            }
        assertEquals(expectedJson.toString(), json.toString())
    }

    @Test
    fun `PaypalOrderInfoResponse should convert to PaypalOrder correctly`() {
        // Arrange
        val response =
            PaypalOrderInfoResponse(
                orderId = "order123",
                externalPayerInfo =
                    PaypalExternalPayerInfo(
                        externalPayerId = "payer123",
                        email = "email@example.com",
                        firstName = "John",
                        lastName = "Doe",
                    ),
            )

        // Act
        val paypalOrder = response.toPaypalOrder()

        // Assert
        val expectedPaypalOrder =
            PaypalOrderInfo(
                orderId = "order123",
                email = "email@example.com",
                externalPayerId = "payer123",
                externalPayerFirstName = "John",
                externalPayerLastName = "Doe",
            )
        assertEquals(expectedPaypalOrder, paypalOrder)
    }
}

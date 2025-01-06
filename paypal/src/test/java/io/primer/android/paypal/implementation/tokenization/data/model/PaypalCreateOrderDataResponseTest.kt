package io.primer.android.paypal.implementation.tokenization.data.model

import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrder
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaypalCreateOrderDataResponseTest {
    @Test
    fun `PaypalCreateOrderDataResponse should deserialize correctly`() {
        // Arrange
        val orderId = "order123"
        val approvalUrl = "https://example.com/approval"
        val json =
            JSONObject().apply {
                put("orderId", orderId)
                put("approvalUrl", approvalUrl)
            }

        // Act
        val response = PaypalCreateOrderDataResponse.deserializer.deserialize(json)

        // Assert
        val expectedResponse = PaypalCreateOrderDataResponse(orderId, approvalUrl)
        assertEquals(expectedResponse, response)
    }

    @Test
    fun `PaypalCreateOrderDataResponse should convert to PaypalOrder correctly`() {
        // Arrange
        val orderId = "order123"
        val approvalUrl = "https://example.com/approval"
        val successUrl = "https://example.com/success"
        val cancelUrl = "https://example.com/cancel"
        val response = PaypalCreateOrderDataResponse(orderId, approvalUrl)

        // Act
        val paypalOrder = response.toPaypalOrder(successUrl, cancelUrl)

        // Assert
        val expectedOrder = PaypalOrder(orderId, approvalUrl, successUrl, cancelUrl)
        assertEquals(expectedOrder, paypalOrder)
    }
}

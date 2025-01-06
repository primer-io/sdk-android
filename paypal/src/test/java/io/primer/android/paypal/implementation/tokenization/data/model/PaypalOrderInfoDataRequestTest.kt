package io.primer.android.paypal.implementation.tokenization.data.model

import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalOrderInfoParams
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaypalOrderInfoDataRequestTest {
    @Test
    fun `PaypalOrderInfoDataRequest should serialize correctly`() {
        // Arrange
        val paymentMethodConfigId = "config123"
        val orderId = "order123"
        val request = PaypalOrderInfoDataRequest(paymentMethodConfigId, orderId)

        // Act
        val json = PaypalOrderInfoDataRequest.serializer.serialize(request)

        // Assert
        val expectedJson =
            JSONObject().apply {
                put("paymentMethodConfigId", paymentMethodConfigId)
                put("orderId", orderId)
            }
        assertEquals(expectedJson.toString(), json.toString())
    }

    @Test
    fun `PaypalOrderInfoParams should convert to PaypalOrderInfoDataRequest correctly`() {
        // Arrange
        val paymentMethodConfigId = "config123"
        val orderId = "order123"
        val params = PaypalOrderInfoParams(paymentMethodConfigId, orderId)

        // Act
        val request = params.toPaypalOrderInfoRequest()

        // Assert
        val expectedRequest = PaypalOrderInfoDataRequest(paymentMethodConfigId, orderId)
        assertEquals(expectedRequest, request)
    }
}

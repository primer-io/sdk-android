package io.primer.android.paypal.implementation.tokenization.data.model

import io.mockk.mockkStatic
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PaypalCheckoutPaymentInstrumentDataRequestTest {
    @BeforeEach
    fun setUp() {
        mockkStatic(JSONSerializationUtils::class)
    }

    @Test
    fun `PaypalCheckoutPaymentInstrumentDataRequest should serialize correctly`() {
        // Arrange
        val paypalOrderId = "order123"
        val externalPayerInfo =
            ExternalPayerInfoRequest(
                email = "email@example.com",
                externalPayerId = "payer123",
                firstName = "John",
                lastName = "Doe",
            )
        val request =
            PaypalPaymentInstrumentDataRequest.PaypalCheckoutPaymentInstrumentDataRequest(
                paypalOrderId = paypalOrderId,
                externalPayerInfo = externalPayerInfo,
            )

        // Act
        val json =
            PaypalPaymentInstrumentDataRequest.PaypalCheckoutPaymentInstrumentDataRequest.serializer.serialize(request)

        // Assert
        val expectedJson =
            JSONObject().apply {
                put("paypalOrderId", paypalOrderId)
                put(
                    "externalPayerInfo",
                    JSONObject()
                        .apply {
                            put("email", externalPayerInfo.email)
                            put("external_payer_id", externalPayerInfo.externalPayerId)
                            put("first_name", externalPayerInfo.firstName)
                            put("last_name", externalPayerInfo.lastName)
                        },
                )
            }
        assertEquals(expectedJson.toString(), json.toString())
    }
}

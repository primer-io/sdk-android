package io.primer.android.paypal.implementation.tokenization.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaypalCreateBillingAgreementDataRequestTest {
    @Test
    fun `PaypalCreateBillingAgreementDataRequest should serialize correctly`() {
        // Arrange
        val paymentMethodConfigId = "config123"
        val returnUrl = "https://example.com/return"
        val cancelUrl = "https://example.com/cancel"
        val request =
            PaypalCreateBillingAgreementDataRequest(
                paymentMethodConfigId = paymentMethodConfigId,
                returnUrl = returnUrl,
                cancelUrl = cancelUrl,
            )

        // Act
        val json = PaypalCreateBillingAgreementDataRequest.serializer.serialize(request)

        // Assert
        val expectedJson =
            JSONObject().apply {
                put("paymentMethodConfigId", paymentMethodConfigId)
                put("returnUrl", returnUrl)
                put("cancelUrl", cancelUrl)
            }
        assertEquals(expectedJson.toString(), json.toString())
    }

    @Test
    fun `WhitelistedHttpBodyKeysProvider should return correct keys`() {
        // Arrange
        val expectedKeys = listOf("paymentMethodConfigId", "returnUrl", "cancelUrl")

        // Act
        val actualKeys = PaypalCreateBillingAgreementDataRequest.provider.values.map { it.value }

        // Assert
        assertEquals(expectedKeys, actualKeys)
    }
}

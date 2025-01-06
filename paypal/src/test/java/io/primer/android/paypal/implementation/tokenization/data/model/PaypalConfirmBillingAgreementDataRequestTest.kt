package io.primer.android.paypal.implementation.tokenization.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaypalConfirmBillingAgreementDataRequestTest {
    @Test
    fun `PaypalConfirmBillingAgreementDataRequest should serialize correctly`() {
        // Arrange
        val paymentMethodConfigId = "config123"
        val tokenId = "token123"
        val request =
            PaypalConfirmBillingAgreementDataRequest(
                paymentMethodConfigId = paymentMethodConfigId,
                tokenId = tokenId,
            )

        // Act
        val json = PaypalConfirmBillingAgreementDataRequest.serializer.serialize(request)

        // Assert
        val expectedJson =
            JSONObject().apply {
                put("paymentMethodConfigId", paymentMethodConfigId)
                put("tokenId", tokenId)
            }
        assertEquals(expectedJson.toString(), json.toString())
    }

    @Test
    fun `provider should return the correct whitelisted keys`() {
        // Act
        val whitelistedKeys = PaypalConfirmBillingAgreementDataRequest.provider.values

        // Assert
        assertEquals(1, whitelistedKeys.size)
        assertEquals("paymentMethodConfigId", whitelistedKeys.first().value)
    }
}

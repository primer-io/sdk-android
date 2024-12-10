package io.primer.android.paypal.implementation.tokenization.data.model

import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalBillingAgreement
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaypalCreateBillingAgreementDataResponseTest {

    @Test
    fun `PaypalCreateBillingAgreementDataResponse should deserialize correctly`() {
        // Arrange
        val json = JSONObject().apply {
            put("tokenId", "token123")
            put("approvalUrl", "https://example.com/approval")
        }

        // Act
        val response = PaypalCreateBillingAgreementDataResponse.deserializer.deserialize(json)

        // Assert
        assertEquals("token123", response.tokenId)
        assertEquals("https://example.com/approval", response.approvalUrl)
    }

    @Test
    fun `toBillingAgreement should convert correctly`() {
        // Arrange
        val response = PaypalCreateBillingAgreementDataResponse(
            tokenId = "token123",
            approvalUrl = "https://example.com/approval"
        )
        val paymentMethodConfigId = "config123"
        val successUrl = "https://example.com/success"
        val cancelUrl = "https://example.com/cancel"

        // Act
        val billingAgreement = response.toBillingAgreement(paymentMethodConfigId, successUrl, cancelUrl)

        // Assert
        val expectedBillingAgreement = PaypalBillingAgreement(
            paymentMethodConfigId = paymentMethodConfigId,
            approvalUrl = "https://example.com/approval",
            successUrl = successUrl,
            cancelUrl = cancelUrl
        )
        assertEquals(expectedBillingAgreement, billingAgreement)
    }
}

package io.primer.android.paypal.implementation.tokenization.data.model

import io.primer.android.core.logging.internal.WhitelistedKey
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaypalCreateOrderDataRequestTest {

    @Test
    fun `PaypalCreateOrderDataRequest should serialize correctly`() {
        // Arrange
        val paymentMethodConfigId = "config123"
        val amount = 100
        val currencyCode = "USD"
        val returnUrl = "https://example.com/return"
        val cancelUrl = "https://example.com/cancel"
        val request = PaypalCreateOrderDataRequest(
            paymentMethodConfigId = paymentMethodConfigId,
            amount = amount,
            currencyCode = currencyCode,
            returnUrl = returnUrl,
            cancelUrl = cancelUrl
        )

        // Act
        val json = PaypalCreateOrderDataRequest.serializer.serialize(request)

        // Assert
        val expectedJson = JSONObject().apply {
            put("paymentMethodConfigId", paymentMethodConfigId)
            putOpt("amount", amount)
            putOpt("currencyCode", currencyCode)
            put("returnUrl", returnUrl)
            put("cancelUrl", cancelUrl)
        }
        assertEquals(expectedJson.toString(), json.toString())
    }

    @Test
    fun `Whitelisted keys should be correctly provided`() {
        // Act
        val whitelistedKeys = PaypalCreateOrderDataRequest.provider.values

        // Assert
        val expectedKeys = listOf(
            WhitelistedKey.PrimitiveWhitelistedKey("paymentMethodConfigId"),
            WhitelistedKey.PrimitiveWhitelistedKey("amount"),
            WhitelistedKey.PrimitiveWhitelistedKey("currencyCode"),
            WhitelistedKey.PrimitiveWhitelistedKey("returnUrl"),
            WhitelistedKey.PrimitiveWhitelistedKey("cancelUrl")
        )
        assertEquals(expectedKeys, whitelistedKeys)
    }
}

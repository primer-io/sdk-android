package io.primer.android.payments.core.tokenization.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BasePaymentTokenTest {

    @Test
    fun `test deserialization of VaultDataResponse`() {
        // Given
        val json = JSONObject().apply {
            put("customerId", "test_customer_id")
        }

        // When
        val vaultData = BasePaymentToken.VaultDataResponse.deserializer.deserialize(json)

        // Then
        assertEquals("test_customer_id", vaultData.customerId)
    }

    @Test
    fun `test deserialization of AuthenticationDetailsDataResponse`() {
        // Given
        val json = JSONObject().apply {
            put("responseCode", "AUTH_SUCCESS")
            putOpt("reasonCode", "123")
            putOpt("reasonText", "Test reason")
            putOpt("protocolVersion", "1.0")
            putOpt("challengeIssued", true)
        }

        // When
        val authDetails = BasePaymentToken.AuthenticationDetailsDataResponse.deserializer.deserialize(json)

        // Then
        assertEquals(ResponseCode.AUTH_SUCCESS, authDetails.responseCode)
        assertEquals("123", authDetails.reasonCode)
        assertEquals("Test reason", authDetails.reasonText)
        assertEquals("1.0", authDetails.protocolVersion)
        assertEquals(true, authDetails.challengeIssued)
    }
}

package io.primer.android.klarna.implementation.session.data.models

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class FinalizeKlarnaSessionDataRequestTest {

    @Test
    fun `serializer should correctly serialize FinalizeKlarnaSessionDataRequest`() {
        // Given
        val paymentMethodConfigId = "test-payment-method-config-id"
        val sessionId = "test-session-id"
        val request = FinalizeKlarnaSessionDataRequest(paymentMethodConfigId, sessionId)

        // When
        val jsonObject = FinalizeKlarnaSessionDataRequest.serializer.serialize(request)

        // Then
        assertEquals(paymentMethodConfigId, jsonObject.getString("paymentMethodConfigId"))
        assertEquals(sessionId, jsonObject.getString("sessionId"))
    }

    @Test
    fun `serialized JSONObject should contain all required fields`() {
        // Given
        val paymentMethodConfigId = "test-payment-method-config-id"
        val sessionId = "test-session-id"
        val request = FinalizeKlarnaSessionDataRequest(paymentMethodConfigId, sessionId)

        // When
        val jsonObject = FinalizeKlarnaSessionDataRequest.serializer.serialize(request)

        // Then
        val expectedJson = JSONObject().apply {
            put("paymentMethodConfigId", paymentMethodConfigId)
            put("sessionId", sessionId)
        }
        assertEquals(expectedJson.toString(), jsonObject.toString())
    }
}

package io.primer.android.payments.core.status.data.models

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AsyncPaymentMethodStatusDataResponseTest {
    @Test
    fun `deserializer should correctly parse JSON`() {
        // Given
        val json =
            JSONObject()
                .put("id", "test_id")
                .put("status", "COMPLETE")
                .put("source", "test_source")

        // When
        val result = AsyncPaymentMethodStatusDataResponse.deserializer.deserialize(json)

        // Then
        assertEquals("test_id", result.id)
        assertEquals(AsyncMethodStatus.COMPLETE, result.status)
        assertEquals("test_source", result.source)
    }
}

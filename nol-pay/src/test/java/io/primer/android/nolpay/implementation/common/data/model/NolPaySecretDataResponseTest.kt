package io.primer.android.nolpay.implementation.common.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class NolPaySecretDataResponseTest {

    @Test
    fun `deserializer should correctly deserialize NolPaySecretDataResponse`() {
        // Given
        val sdkSecret = "secret-123"
        val json = JSONObject().apply {
            put("sdkSecret", sdkSecret)
        }

        // When
        val actualResponse = NolPaySecretDataResponse.deserializer.deserialize(json)

        // Then
        val expectedResponse = NolPaySecretDataResponse(sdkSecret)
        assertEquals(expectedResponse, actualResponse)
    }
}

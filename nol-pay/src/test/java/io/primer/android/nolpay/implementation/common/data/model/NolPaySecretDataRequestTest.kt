package io.primer.android.nolpay.implementation.common.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class NolPaySecretDataRequestTest {

    @Test
    fun `serializer should correctly serialize NolPaySecretDataRequest`() {
        // Given
        val sdkId = "sdk-123"
        val appId = "app-456"
        val deviceVendor = "Google"
        val deviceModel = "Pixel 5"
        val request = NolPaySecretDataRequest(sdkId, appId, deviceVendor, deviceModel)

        // When
        val json = NolPaySecretDataRequest.serializer.serialize(request)

        // Then
        val expectedJson = JSONObject().apply {
            put("nolSdkId", sdkId)
            put("nolAppId", appId)
            put("phoneVendor", deviceVendor)
            put("phoneModel", deviceModel)
        }
        assertEquals(expectedJson.toString(), json.toString())
    }

    @Test
    fun `provider should return correct whitelisted keys`() {
        // Given
        val expectedKeys = listOf("phoneVendor", "phoneModel")

        // When
        val keys = NolPaySecretDataRequest.provider.values.map { it.value }

        // Then
        assertEquals(expectedKeys, keys)
    }
}

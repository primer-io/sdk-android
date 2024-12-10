package io.primer.android.qrcode.implementation.tokenization.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class QrCodeSessionInfoDataRequestTest {

    @Test
    fun `test QrCodeSessionInfoDataRequest serialization`() {
        val sessionInfo = QrCodeSessionInfoDataRequest(
            locale = "en-US",
            platform = "ANDROID"
        )

        val expectedJson = JSONObject().apply {
            put("platform", "ANDROID")
            put("locale", "en-US")
        }

        val serializedJson = QrCodeSessionInfoDataRequest.serializer.serialize(sessionInfo)
        assertEquals(expectedJson.toString(), serializedJson.toString())
    }

    @Test
    fun `test QrCodeSessionInfoDataRequest default platform serialization`() {
        val sessionInfo = QrCodeSessionInfoDataRequest(
            locale = "en-US"
        )

        val expectedJson = JSONObject().apply {
            put("platform", "ANDROID")
            put("locale", "en-US")
        }

        val serializedJson = QrCodeSessionInfoDataRequest.serializer.serialize(sessionInfo)
        assertEquals(expectedJson.toString(), serializedJson.toString())
    }
}

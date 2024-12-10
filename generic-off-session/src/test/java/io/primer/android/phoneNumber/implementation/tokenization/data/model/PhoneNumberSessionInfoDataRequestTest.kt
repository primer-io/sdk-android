package io.primer.android.phoneNumber.implementation.tokenization.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PhoneNumberSessionInfoDataRequestTest {

    @Test
    fun `test PhoneNumberSessionInfoDataRequest serialization`() {
        val sessionInfo = PhoneNumberSessionInfoDataRequest(
            locale = "en-US",
            phoneNumber = "1234567890",
            platform = "ANDROID"
        )

        val expectedJson = JSONObject().apply {
            put("platform", "ANDROID")
            put("locale", "en-US")
            put("phoneNumber", "1234567890")
        }

        val serializedJson = PhoneNumberSessionInfoDataRequest.serializer.serialize(sessionInfo)
        assertEquals(expectedJson.toString(), serializedJson.toString())
    }

    @Test
    fun `test PhoneNumberSessionInfoDataRequest default platform serialization`() {
        val sessionInfo = PhoneNumberSessionInfoDataRequest(
            locale = "en-US",
            phoneNumber = "1234567890"
        )

        val expectedJson = JSONObject().apply {
            put("platform", "ANDROID")
            put("locale", "en-US")
            put("phoneNumber", "1234567890")
        }

        val serializedJson = PhoneNumberSessionInfoDataRequest.serializer.serialize(sessionInfo)
        assertEquals(expectedJson.toString(), serializedJson.toString())
    }
}

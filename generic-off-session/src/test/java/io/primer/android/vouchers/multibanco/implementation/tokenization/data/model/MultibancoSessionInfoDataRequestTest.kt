package io.primer.android.vouchers.multibanco.implementation.tokenization.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class MultibancoSessionInfoDataRequestTest {
    @Test
    fun `test MultibancoSessionInfoDataRequest serialization`() {
        val sessionInfo =
            MultibancoSessionInfoDataRequest(
                locale = "en-US",
                platform = "ANDROID",
            )

        val expectedJson =
            JSONObject().apply {
                put("platform", "ANDROID")
                put("locale", "en-US")
            }

        val serializedJson = MultibancoSessionInfoDataRequest.serializer.serialize(sessionInfo)
        assertEquals(expectedJson.toString(), serializedJson.toString())
    }

    @Test
    fun `test MultibancoSessionInfoDataRequest default platform serialization`() {
        val sessionInfo =
            MultibancoSessionInfoDataRequest(
                locale = "en-US",
            )

        val expectedJson =
            JSONObject().apply {
                put("platform", "ANDROID")
                put("locale", "en-US")
            }

        val serializedJson = MultibancoSessionInfoDataRequest.serializer.serialize(sessionInfo)
        assertEquals(expectedJson.toString(), serializedJson.toString())
    }
}

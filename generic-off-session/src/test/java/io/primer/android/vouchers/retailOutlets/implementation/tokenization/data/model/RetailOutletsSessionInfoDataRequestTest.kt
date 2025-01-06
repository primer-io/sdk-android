package io.primer.android.vouchers.retailOutlets.implementation.tokenization.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class RetailOutletsSessionInfoDataRequestTest {
    @Test
    fun `test RetailOutletsSessionInfoDataRequest serialization`() {
        val sessionInfo =
            RetailOutletsSessionInfoDataRequest(
                locale = "en-US",
                platform = "ANDROID",
                retailerOutlet = "testRetailOutlet",
            )

        val expectedJson =
            JSONObject().apply {
                put("platform", "ANDROID")
                put("locale", "en-US")
                put("retailOutlet", "testRetailOutlet")
            }

        val serializedJson = RetailOutletsSessionInfoDataRequest.serializer.serialize(sessionInfo)
        assertEquals(expectedJson.toString(), serializedJson.toString())
    }

    @Test
    fun `test RetailOutletsSessionInfoDataRequest default platform serialization`() {
        val sessionInfo =
            RetailOutletsSessionInfoDataRequest(
                locale = "en-US",
                retailerOutlet = "testRetailOutlet",
            )

        val expectedJson =
            JSONObject().apply {
                put("platform", "ANDROID")
                put("locale", "en-US")
                put("retailOutlet", "testRetailOutlet")
            }

        val serializedJson = RetailOutletsSessionInfoDataRequest.serializer.serialize(sessionInfo)
        assertEquals(expectedJson.toString(), serializedJson.toString())
    }
}

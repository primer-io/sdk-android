package io.primer.android.nolpay.implementation.paymentCard.tokenization.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class NolPaySessionInfoDataRequestTest {
    @Test
    fun `serializer should correctly serialize NolPaySessionInfoDataRequest to JSONObject`() {
        val sessionInfo =
            NolPaySessionInfoDataRequest(
                mobileCountryCode = "US",
                mobileNumber = "1234567890",
                nolPayCardNumber = "1234567812345678",
                deviceVendor = "TestManufacturer",
                deviceModel = "TestModel",
            )

        val expectedJson =
            JSONObject().apply {
                put("platform", "ANDROID")
                put("mobileCountryCode", "US")
                put("mobileNumber", "1234567890")
                put("nolPayCardNumber", "1234567812345678")
                put("phoneVendor", "TestManufacturer")
                put("phoneModel", "TestModel")
            }

        val serializedJson = NolPaySessionInfoDataRequest.serializer.serialize(sessionInfo)

        assertEquals(expectedJson.toString(), serializedJson.toString())
    }
}

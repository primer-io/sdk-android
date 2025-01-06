package io.primer.android.klarna.implementation.session.data.models

import io.mockk.mockkStatic
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class FinalizeKlarnaSessionDataResponseTest {
    @BeforeEach
    fun setUp() {
        mockkStatic(JSONSerializationUtils::class)
    }

    @Test
    fun `deserializer should correctly deserialize FinalizeKlarnaSessionDataResponse`() {
        // Given
        val sessionData =
            KlarnaSessionData(
                recurringDescription = "recurringDescription",
                purchaseCountry = "purchaseCountry",
                purchaseCurrency = "purchaseCurrency",
                locale = "locale",
                orderAmount = 100,
                orderLines = emptyList(),
                billingAddress = null,
                shippingAddress = null,
                tokenDetails = null,
                orderTaxAmount = 10,
            )

        val sessionDataJson =
            JSONObject().apply {
                put("recurringDescription", "recurringDescription")
                put("purchaseCountry", "purchaseCountry")
                put("purchaseCurrency", "purchaseCurrency")
                put("locale", "locale")
                put("orderAmount", 100)
                put("orderTaxAmount", 10)
                put("orderLines", JSONArray())
                put("billingAddress", JSONObject.NULL)
                put("shippingAddress", JSONObject.NULL)
                put("tokenDetails", JSONObject.NULL)
            }

        val responseJson =
            JSONObject().apply {
                put("sessionData", sessionDataJson)
            }

        // When
        val response = FinalizeKlarnaSessionDataResponse.deserializer.deserialize(responseJson)

        // Then
        assertEquals(sessionData, response.sessionData)
    }
}

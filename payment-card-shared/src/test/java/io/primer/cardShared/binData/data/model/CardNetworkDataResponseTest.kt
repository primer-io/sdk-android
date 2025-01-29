package io.primer.cardShared.binData.data.model

import io.primer.android.configuration.data.model.CardNetwork
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CardNetworkDataResponseTest {
    @Test
    fun `CardNetworkDataResponse deserialization works correctly`() {
        // Create a sample JSON object representing a CardNetworkDataResponse
        val json =
            JSONObject().apply {
                put(CardNetworkDataResponse.DISPLAY_NAME_FIELD, "Visa")
                put(CardNetworkDataResponse.VALUE_FIELD, "VISA")
            }

        // Deserialize the JSON object to a CardNetworkDataResponse instance
        val deserializedObject = CardNetworkDataResponse.deserializer.deserialize(json)

        // Create the expected CardNetworkDataResponse instance
        val expectedObject =
            CardNetworkDataResponse(
                displayName = "Visa",
                value = CardNetwork.Type.VISA,
            )

        // Verify that the deserialized object matches the expected object
        assertEquals(expectedObject, deserializedObject)
    }
}

class CardBinMetadataDataNetworksResponseTest {
    @Test
    fun `CardBinMetadataDataNetworksResponse deserialization works correctly`() {
        val networkJson =
            JSONObject().apply {
                put(CardNetworkDataResponse.DISPLAY_NAME_FIELD, "Visa")
                put(CardNetworkDataResponse.VALUE_FIELD, "VISA")
            }
        val jsonArray = JSONArray().apply { put(networkJson) }
        val json = JSONObject().apply { put(CardBinMetadataDataNetworksResponse.NETWORKS_FIELD, jsonArray) }

        val deserializedObject = CardBinMetadataDataNetworksResponse.deserializer.deserialize(json)

        val expectedObject =
            CardBinMetadataDataNetworksResponse(
                networks =
                listOf(
                    CardNetworkDataResponse(
                        displayName = "Visa",
                        value = CardNetwork.Type.VISA,
                    ),
                ),
            )

        assertEquals(expectedObject, deserializedObject)
    }
}

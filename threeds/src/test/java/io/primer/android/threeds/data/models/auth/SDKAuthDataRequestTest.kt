package io.primer.android.threeds.data.models.auth

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SDKAuthDataRequestTest {

    @Test
    fun `serializer should serialize SDKAuthDataRequest to JSONObject correctly`() {
        // Define the repeating values
        val sdkAppId = "app-id"
        val sdkTransactionId = "transaction-id"
        val sdkTimeout = 60
        val sdkEncData = "encrypted-data"
        val sdkEphemPubKey = "ephemeral-public-key"
        val sdkReferenceNumber = "reference-number"

        // Create an instance of SDKAuthDataRequest with these values
        val sdkAuthDataRequest = SDKAuthDataRequest(
            sdkAppId = sdkAppId,
            sdkTransactionId = sdkTransactionId,
            sdkTimeout = sdkTimeout,
            sdkEncData = sdkEncData,
            sdkEphemPubKey = sdkEphemPubKey,
            sdkReferenceNumber = sdkReferenceNumber
        )

        // Serialize the SDKAuthDataRequest instance to JSONObject
        val jsonObject = SDKAuthDataRequest.serializer.serialize(sdkAuthDataRequest)

        // Create an expected JSONObject with the same values
        val expectedJsonObject = JSONObject().apply {
            put("sdkAppId", sdkAppId)
            put("sdkTransactionId", sdkTransactionId)
            put("sdkTimeout", sdkTimeout)
            put("sdkEncData", sdkEncData)
            put("sdkEphemPubKey", sdkEphemPubKey)
            put("sdkReferenceNumber", sdkReferenceNumber)
        }

        // Assert the serialized JSONObject matches the expected values
        assertEquals(expectedJsonObject.toString(), jsonObject.toString())
    }
}

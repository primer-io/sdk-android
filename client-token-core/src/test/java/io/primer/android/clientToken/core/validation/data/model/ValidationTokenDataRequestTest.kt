package io.primer.android.clientToken.core.validation.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ValidationTokenDataRequestTest {

    @Test
    fun `serializer should return JSONObject with clientToken field`() {
        // Arrange
        val clientToken = "sample_token"
        val validationTokenDataRequest = ValidationTokenDataRequest(clientToken)

        // Act
        val jsonObject: JSONObject = ValidationTokenDataRequest.serializer.serialize(validationTokenDataRequest)

        // Assert
        assertEquals(clientToken, jsonObject.getString("clientToken"))
    }

    @Test
    fun `toValidationTokenData should create ValidationTokenDataRequest with clientToken`() {
        // Arrange
        val clientToken = "sample_token"

        // Act
        val validationTokenDataRequest = clientToken.toValidationTokenData()

        // Assert
        assertEquals(clientToken, validationTokenDataRequest.clientToken)
    }
}

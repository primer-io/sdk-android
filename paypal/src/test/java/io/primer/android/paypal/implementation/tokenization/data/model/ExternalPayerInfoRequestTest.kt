package io.primer.android.paypal.implementation.tokenization.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ExternalPayerInfoRequestTest {

    @Test
    fun `ExternalPayerInfoRequest should serialize correctly`() {
        // Arrange
        val email = "email@example.com"
        val externalPayerId = "payer123"
        val firstName = "John"
        val lastName = "Doe"

        val externalPayerInfoRequest = ExternalPayerInfoRequest(
            email = email,
            externalPayerId = externalPayerId,
            firstName = firstName,
            lastName = lastName
        )

        // Act
        val json = ExternalPayerInfoRequest.serializer.serialize(externalPayerInfoRequest)

        // Assert
        val expectedJson = JSONObject().apply {
            put("email", email)
            put("external_payer_id", externalPayerId)
            put("first_name", firstName)
            put("last_name", lastName)
        }
        assertEquals(expectedJson.toString(), json.toString())
    }

    @Test
    fun `ExternalPayerInfoRequest should handle null values correctly`() {
        // Arrange
        val externalPayerInfoRequest = ExternalPayerInfoRequest(
            email = null,
            externalPayerId = null,
            firstName = null,
            lastName = null
        )

        // Act
        val json = ExternalPayerInfoRequest.serializer.serialize(externalPayerInfoRequest)

        // Assert
        val expectedJson = JSONObject()
        assertEquals(expectedJson.toString(), json.toString())
    }
}

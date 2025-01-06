package io.primer.android.phoneMetadata.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PhoneMetadataResponseTest {
    @Test
    fun `test deserialization with valid fields`() {
        val json = """
            {
                "isValid": true,
                "countryCode": "US",
                "nationalNumber": "1234567890"
            }
        """
        val jsonObject = JSONObject(json)
        val response = PhoneMetadataResponse.deserializer.deserialize(jsonObject)

        assertEquals(true, response.isValid)
        assertEquals("US", response.countryCode)
        assertEquals("1234567890", response.nationalNumber)
    }

    @Test
    fun `test deserialization with null countryCode and nationalNumber`() {
        val json = """
            {
                "isValid": false,
                "countryCode": null,
                "nationalNumber": null
            }
        """
        val jsonObject = JSONObject(json)
        val response = PhoneMetadataResponse.deserializer.deserialize(jsonObject)

        assertEquals(false, response.isValid)
        assertEquals(null, response.countryCode)
        assertEquals(null, response.nationalNumber)
    }

    @Test
    fun `test deserialization with missing countryCode and nationalNumber`() {
        val json = """
            {
                "isValid": true
            }
        """
        val jsonObject = JSONObject(json)
        val response = PhoneMetadataResponse.deserializer.deserialize(jsonObject)

        assertEquals(true, response.isValid)
        assertEquals(null, response.countryCode)
        assertEquals(null, response.nationalNumber)
    }

    @Test
    fun `test deserialization with additional unexpected fields`() {
        val json = """
            {
                "isValid": true,
                "countryCode": "US",
                "nationalNumber": "1234567890",
                "unexpectedField": "unexpectedValue"
            }
        """
        val jsonObject = JSONObject(json)
        val response = PhoneMetadataResponse.deserializer.deserialize(jsonObject)

        assertEquals(true, response.isValid)
        assertEquals("US", response.countryCode)
        assertEquals("1234567890", response.nationalNumber)
    }
}

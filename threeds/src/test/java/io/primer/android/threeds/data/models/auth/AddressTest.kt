package io.primer.android.threeds.data.models.auth

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AddressTest {

    @Test
    fun `serializer should serialize Address to JSONObject correctly`() {
        val address = Address(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phoneNumber = "1234567890",
            addressLine1 = "123 Main St",
            addressLine2 = "Apt 4B",
            addressLine3 = null,
            city = "Anytown",
            state = "Anystate",
            countryCode = "US",
            postalCode = "12345"
        )

        val jsonObject = Address.serializer.serialize(address)

        assertEquals("John", jsonObject.optString("firstName"))
        assertEquals("Doe", jsonObject.optString("lastName"))
        assertEquals("john.doe@example.com", jsonObject.optString("email"))
        assertEquals("1234567890", jsonObject.optString("phoneNumber"))
        assertEquals("123 Main St", jsonObject.optString("addressLine1"))
        assertEquals("Apt 4B", jsonObject.optString("addressLine2"))
        assertEquals("", jsonObject.optString("addressLine3"))
        assertEquals("Anytown", jsonObject.optString("city"))
        assertEquals("Anystate", jsonObject.optString("state"))
        assertEquals("US", jsonObject.optString("countryCode"))
        assertEquals("12345", jsonObject.optString("postalCode"))
    }

    @Test
    fun `serializer should handle null optional fields`() {
        val address = Address(
            firstName = null,
            lastName = null,
            email = null,
            phoneNumber = null,
            addressLine1 = "123 Main St",
            addressLine2 = null,
            addressLine3 = null,
            city = "Anytown",
            state = null,
            countryCode = "US",
            postalCode = "12345"
        )

        val jsonObject = Address.serializer.serialize(address)

        assertEquals("", jsonObject.optString("firstName"))
        assertEquals("", jsonObject.optString("lastName"))
        assertEquals("", jsonObject.optString("email"))
        assertEquals("", jsonObject.optString("phoneNumber"))
        assertEquals("123 Main St", jsonObject.optString("addressLine1"))
        assertEquals("", jsonObject.optString("addressLine2"))
        assertEquals("", jsonObject.optString("addressLine3"))
        assertEquals("Anytown", jsonObject.optString("city"))
        assertEquals("", jsonObject.optString("state"))
        assertEquals("US", jsonObject.optString("countryCode"))
        assertEquals("12345", jsonObject.optString("postalCode"))
    }
}

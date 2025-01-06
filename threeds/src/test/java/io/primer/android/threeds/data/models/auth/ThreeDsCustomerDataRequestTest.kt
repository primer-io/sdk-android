package io.primer.android.threeds.data.models.auth

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ThreeDsCustomerDataRequestTest {
    @Test
    fun `serializer should serialize ThreeDsCustomerDataRequest to JSONObject correctly`() {
        // Define the repeating values
        val name = "John Doe"
        val email = "john.doe@example.com"
        val homePhone = "123-456-7890"
        val mobilePhone = "098-765-4321"
        val workPhone = "111-222-3333"

        // Create an instance of ThreeDsCustomerDataRequest with these values
        val customerDataRequest =
            ThreeDsCustomerDataRequest(
                name = name,
                email = email,
                homePhone = homePhone,
                mobilePhone = mobilePhone,
                workPhone = workPhone,
            )

        // Serialize the ThreeDsCustomerDataRequest instance to JSONObject
        val jsonObject = ThreeDsCustomerDataRequest.serializer.serialize(customerDataRequest)

        // Create an expected JSONObject with the same values
        val expectedJsonObject =
            JSONObject().apply {
                putOpt("name", name)
                putOpt("email", email)
                putOpt("homePhone", homePhone)
                putOpt("mobilePhone", mobilePhone)
                putOpt("workPhone", workPhone)
            }

        // Assert the serialized JSONObject matches the expected values
        assertEquals(expectedJsonObject.toString(), jsonObject.toString())
    }

    @Test
    fun `serializer should handle null values correctly`() {
        // Create an instance of ThreeDsCustomerDataRequest with null values
        val customerDataRequest = ThreeDsCustomerDataRequest()

        // Serialize the ThreeDsCustomerDataRequest instance to JSONObject
        val jsonObject = ThreeDsCustomerDataRequest.serializer.serialize(customerDataRequest)

        // Create an expected JSONObject with null values
        val expectedJsonObject =
            JSONObject().apply {
                putOpt("name", null)
                putOpt("email", null)
                putOpt("homePhone", null)
                putOpt("mobilePhone", null)
                putOpt("workPhone", null)
            }

        // Assert the serialized JSONObject matches the expected values
        assertEquals(expectedJsonObject.toString(), jsonObject.toString())
    }
}

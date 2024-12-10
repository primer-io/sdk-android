package io.primer.android.threeds.data.models.auth

import io.primer.android.threeds.domain.models.ThreeDsCheckoutParams
import io.primer.android.threeds.helpers.ProtocolVersion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class BeginAuthDataRequestTest {

    @Test
    fun `serializer should serialize BeginAuthDataRequest to JSONObject correctly`() {
        val device = SDKAuthDataRequest(
            sdkAppId = "sdkAppId",
            sdkTransactionId = "sdkTransactionId",
            sdkTimeout = 60,
            sdkEncData = "sdkEncData",
            sdkEphemPubKey = "sdkEphemPubKey",
            sdkReferenceNumber = "sdkReferenceNumber"
        )

        val billingAddress = Address(
            firstName = "John",
            lastName = "Doe",
            email = "john.doe@example.com",
            phoneNumber = "1234567890",
            addressLine1 = "123 Main St",
            addressLine2 = "Apt 4B",
            city = "Anytown",
            state = "Anystate",
            countryCode = "US",
            postalCode = "12345"
        )

        val request = BeginAuthDataRequest(
            maxProtocolVersion = "2.1.0",
            amount = 100,
            currencyCode = "USD",
            orderId = "orderId",
            customer = null,
            device = device,
            billingAddress = billingAddress,
            shippingAddress = null
        )

        val jsonObject = BeginAuthDataRequest.serializer.serialize(request)

        assertEquals("2.1.0", jsonObject.getString("maxProtocolVersion"))
        assertEquals(100, jsonObject.getInt("amount"))
        assertEquals("USD", jsonObject.getString("currencyCode"))
        assertEquals("orderId", jsonObject.getString("orderId"))

        val deviceJson = jsonObject.getJSONObject("device")
        assertEquals("sdkAppId", deviceJson.getString("sdkAppId"))
        assertEquals("sdkTransactionId", deviceJson.getString("sdkTransactionId"))
        assertEquals(60, deviceJson.getInt("sdkTimeout"))
        assertEquals("sdkEncData", deviceJson.getString("sdkEncData"))
        assertEquals("sdkEphemPubKey", deviceJson.getString("sdkEphemPubKey"))
        assertEquals("sdkReferenceNumber", deviceJson.getString("sdkReferenceNumber"))

        val billingAddressJson = jsonObject.getJSONObject("billingAddress")
        assertEquals("John", billingAddressJson.getString("firstName"))
        assertEquals("Doe", billingAddressJson.getString("lastName"))
        assertEquals("john.doe@example.com", billingAddressJson.getString("email"))
        assertEquals("1234567890", billingAddressJson.getString("phoneNumber"))
        assertEquals("123 Main St", billingAddressJson.getString("addressLine1"))
        assertEquals("Apt 4B", billingAddressJson.getString("addressLine2"))
        assertEquals("Anytown", billingAddressJson.getString("city"))
        assertEquals("Anystate", billingAddressJson.getString("state"))
        assertEquals("US", billingAddressJson.getString("countryCode"))
        assertEquals("12345", billingAddressJson.getString("postalCode"))

        assertEquals(false, jsonObject.has("customer"))
        assertEquals(false, jsonObject.has("shippingAddress"))
    }

    @Test
    fun `toBeginAuthRequest should create BeginAuthDataRequest from ThreeDsCheckoutParams`() {
        val threeDsCheckoutParams = ThreeDsCheckoutParams(
            maxProtocolVersion = ProtocolVersion.V_210,
            sdkAppId = "sdkAppId",
            sdkTransactionId = "sdkTransactionId",
            sdkEncData = "sdkEncData",
            sdkEphemPubKey = "sdkEphemPubKey",
            sdkReferenceNumber = "sdkReferenceNumber"
        )

        val beginAuthDataRequest = threeDsCheckoutParams.toBeginAuthRequest()

        assertEquals("2.1.0", beginAuthDataRequest.maxProtocolVersion)
        assertEquals("sdkAppId", beginAuthDataRequest.device.sdkAppId)
        assertEquals("sdkTransactionId", beginAuthDataRequest.device.sdkTransactionId)
        assertEquals(60, beginAuthDataRequest.device.sdkTimeout)
        assertEquals("sdkEncData", beginAuthDataRequest.device.sdkEncData)
        assertEquals("sdkEphemPubKey", beginAuthDataRequest.device.sdkEphemPubKey)
        assertEquals("sdkReferenceNumber", beginAuthDataRequest.device.sdkReferenceNumber)
    }
}

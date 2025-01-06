package io.primer.android.paypal.implementation.tokenization.data.model

import io.mockk.mockkStatic
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PaypalConfirmBillingAgreementDataResponseTest {
    @BeforeEach
    fun setUp() {
        mockkStatic(JSONSerializationUtils::class)
    }

    @Test
    fun `PaypalConfirmBillingAgreementDataResponse should deserialize correctly`() {
        // Arrange
        val billingAgreementId = "agreement123"
        val externalPayerInfoJson =
            JSONObject().apply {
                put("externalPayerId", "payer123")
                put("email", "email@example.com")
                put("firstName", "John")
                put("lastName", "Doe")
            }
        val shippingAddressJson =
            JSONObject().apply {
                put("firstName", "Jane")
                put("lastName", "Doe")
                put("addressLine1", "123 Main St")
                put("addressLine2", "Apt 4B")
                put("city", "Springfield")
                put("state", "IL")
                put("countryCode", "US")
                put("postalCode", "62701")
            }
        val json =
            JSONObject().apply {
                put("billingAgreementId", billingAgreementId)
                put("externalPayerInfo", externalPayerInfoJson)
                put("shippingAddress", shippingAddressJson)
            }

        val externalPayerInfo =
            PaypalExternalPayerInfo(
                externalPayerId = "payer123",
                email = "email@example.com",
                firstName = "John",
                lastName = "Doe",
            )
        val shippingAddress =
            PaypalShippingAddressDataResponse(
                firstName = "Jane",
                lastName = "Doe",
                addressLine1 = "123 Main St",
                addressLine2 = "Apt 4B",
                city = "Springfield",
                state = "IL",
                countryCode = "US",
                postalCode = "62701",
            )

        // Act
        val result = PaypalConfirmBillingAgreementDataResponse.deserializer.deserialize(json)

        // Assert
        assertEquals(billingAgreementId, result.billingAgreementId)
        assertEquals(externalPayerInfo, result.externalPayerInfo)
        assertEquals(shippingAddress, result.shippingAddress)
    }
}

class PaypalShippingAddressDataResponseTest {
    @Test
    fun `PaypalShippingAddressDataResponse should serialize correctly`() {
        // Arrange
        val address =
            PaypalShippingAddressDataResponse(
                firstName = "Jane",
                lastName = "Doe",
                addressLine1 = "123 Main St",
                addressLine2 = "Apt 4B",
                city = "Springfield",
                state = "IL",
                countryCode = "US",
                postalCode = "62701",
            )

        // Act
        val json = PaypalShippingAddressDataResponse.serializer.serialize(address)

        // Assert
        val expectedJson =
            JSONObject().apply {
                put("firstName", "Jane")
                put("lastName", "Doe")
                put("addressLine1", "123 Main St")
                put("addressLine2", "Apt 4B")
                put("city", "Springfield")
                put("state", "IL")
                put("countryCode", "US")
                put("postalCode", "62701")
            }
        assertEquals(expectedJson.toString(), json.toString())
    }

    @Test
    fun `PaypalShippingAddressDataResponse should deserialize correctly`() {
        // Arrange
        val json =
            JSONObject().apply {
                put("firstName", "Jane")
                put("lastName", "Doe")
                put("addressLine1", "123 Main St")
                put("addressLine2", "Apt 4B")
                put("city", "Springfield")
                put("state", "IL")
                put("countryCode", "US")
                put("postalCode", "62701")
            }

        // Act
        val result = PaypalShippingAddressDataResponse.deserializer.deserialize(json)

        // Assert
        val expected =
            PaypalShippingAddressDataResponse(
                firstName = "Jane",
                lastName = "Doe",
                addressLine1 = "123 Main St",
                addressLine2 = "Apt 4B",
                city = "Springfield",
                state = "IL",
                countryCode = "US",
                postalCode = "62701",
            )
        assertEquals(expected, result)
    }
}

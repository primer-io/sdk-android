package io.primer.android.klarna.implementation.session.data.models

import io.primer.android.configuration.data.model.AddressData
import io.primer.android.configuration.data.model.CountryCode
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class KlarnaSessionDataTest {
    @Test
    fun `serializer should correctly serialize KlarnaSessionData`() {
        // Given
        val orderLines =
            listOf(
                SessionOrderLines(
                    type = "physical",
                    name = "Test Item",
                    quantity = 2,
                    reference = "REF123",
                    unitPrice = 100,
                    totalAmount = 200,
                    totalDiscountAmount = 10,
                ),
            )

        val addressData =
            AddressData(
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com",
                phoneNumber = "+1234567890",
                addressLine1 = "123 Main St",
                addressLine2 = "Apt 1",
                addressLine3 = "Floor 2",
                postalCode = "12345",
                city = "New York",
                state = "NY",
                countryCode = CountryCode.US,
            )

        val tokenDetails =
            TokenDetailsData(
                brand = "VISA",
                maskedNumber = "**** **** **** 1234",
                type = "credit",
                expiryDate = "12/23",
            )

        val sessionData =
            KlarnaSessionData(
                recurringDescription = "Recurring payment",
                purchaseCountry = "US",
                purchaseCurrency = "USD",
                locale = "en_US",
                orderAmount = 200,
                orderLines = orderLines,
                billingAddress = addressData,
                shippingAddress = addressData,
                tokenDetails = tokenDetails,
                orderTaxAmount = 20,
            )

        val orderLinesJson =
            JSONArray().put(
                JSONObject().apply {
                    put("type", "physical")
                    put("name", "Test Item")
                    put("quantity", 2)
                    put("reference", "REF123")
                    put("unit_price", 100)
                    put("total_amount", 200)
                    put("total_discount_amount", 10)
                },
            )

        val anAddressJson =
            JSONObject().apply {
                put("firstName", "John")
                put("lastName", "Doe")
                put("email", "john.doe@example.com")
                put("phoneNumber", "+1234567890")
                put("addressLine1", "123 Main St")
                put("addressLine2", "Apt 1")
                put("addressLine3", "Floor 2")
                put("postalCode", "12345")
                put("city", "New York")
                put("state", "NY")
                put("countryCode", "US")
            }

        val tokenDetailsJson =
            JSONObject().apply {
                put("brand", "VISA")
                put("masked_number", "**** **** **** 1234")
                put("type", "credit")
                put("expiry_date", "12/23")
            }

        val expectedJson =
            JSONObject().apply {
                put("recurringDescription", "Recurring payment")
                put("purchaseCountry", "US")
                put("purchaseCurrency", "USD")
                put("locale", "en_US")
                put("orderAmount", 200)
                put("orderLines", orderLinesJson)
                put("billingAddress", anAddressJson)
                put("shippingAddress", anAddressJson)
                put("tokenDetails", tokenDetailsJson)
                put("orderTaxAmount", 20)
            }

        // When
        val actualJson = KlarnaSessionData.serializer.serialize(sessionData)

        // Then
        assertEquals(expectedJson.toString(), actualJson.toString())
    }

    @Test
    fun `deserializer should correctly deserialize KlarnaSessionData`() {
        // Given
        val orderLinesJson =
            JSONArray().put(
                JSONObject().apply {
                    put("type", "physical")
                    put("name", "Test Item")
                    put("quantity", 2)
                    put("reference", "REF123")
                    put("unit_price", 100)
                    put("total_amount", 200)
                    put("total_discount_amount", 10)
                },
            )

        val anAddressJson =
            JSONObject().apply {
                put("firstName", "John")
                put("lastName", "Doe")
                put("email", "john.doe@example.com")
                put("phoneNumber", "+1234567890")
                put("addressLine1", "123 Main St")
                put("addressLine2", "Apt 1")
                put("addressLine3", "Floor 2")
                put("postalCode", "12345")
                put("city", "New York")
                put("state", "NY")
                put("countryCode", "US")
            }

        val tokenDetailsJson =
            JSONObject().apply {
                put("brand", "VISA")
                put("masked_number", "**** **** **** 1234")
                put("type", "credit")
                put("expiry_date", "12/23")
            }

        val json =
            JSONObject().apply {
                put("recurringDescription", "Recurring payment")
                put("purchaseCountry", "US")
                put("purchaseCurrency", "USD")
                put("locale", "en_US")
                put("orderAmount", 200)
                put("orderLines", orderLinesJson)
                put("billingAddress", anAddressJson)
                put("shippingAddress", anAddressJson)
                put("tokenDetails", tokenDetailsJson)
                put("orderTaxAmount", 20)
            }

        val addressData =
            AddressData(
                firstName = "John",
                lastName = "Doe",
                email = "john.doe@example.com",
                phoneNumber = "+1234567890",
                addressLine1 = "123 Main St",
                addressLine2 = "Apt 1",
                addressLine3 = "Floor 2",
                postalCode = "12345",
                city = "New York",
                state = "NY",
                countryCode = CountryCode.US,
            )

        val tokenDetails =
            TokenDetailsData(
                brand = "VISA",
                maskedNumber = "**** **** **** 1234",
                type = "credit",
                expiryDate = "12/23",
            )

        val orderLines =
            listOf(
                SessionOrderLines(
                    type = "physical",
                    name = "Test Item",
                    quantity = 2,
                    reference = "REF123",
                    unitPrice = 100,
                    totalAmount = 200,
                    totalDiscountAmount = 10,
                ),
            )

        val expectedSessionData =
            KlarnaSessionData(
                recurringDescription = "Recurring payment",
                purchaseCountry = "US",
                purchaseCurrency = "USD",
                locale = "en_US",
                orderAmount = 200,
                orderLines = orderLines,
                billingAddress = addressData,
                shippingAddress = addressData,
                tokenDetails = tokenDetails,
                orderTaxAmount = 20,
            )

        // When
        val actualSessionData = KlarnaSessionData.deserializer.deserialize(json)

        // Then
        assertEquals(expectedSessionData, actualSessionData)
    }
}

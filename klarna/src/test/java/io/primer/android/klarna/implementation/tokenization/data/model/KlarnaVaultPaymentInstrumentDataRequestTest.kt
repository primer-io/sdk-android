package io.primer.android.klarna.implementation.tokenization.data.model

import io.primer.android.configuration.data.model.AddressData
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.klarna.implementation.session.data.models.KlarnaSessionData
import io.primer.android.klarna.implementation.session.data.models.SessionOrderLines
import io.primer.android.klarna.implementation.session.data.models.TokenDetailsData
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class KlarnaVaultPaymentInstrumentDataRequestTest {

    @Test
    fun `serializer should correctly serialize KlarnaVaultPaymentInstrumentDataRequest`() {
        // Given
        val orderLines = listOf(
            SessionOrderLines(
                type = "physical",
                name = "Test Item",
                quantity = 2,
                reference = "REF123",
                unitPrice = 100,
                totalAmount = 200,
                totalDiscountAmount = 10
            )
        )

        val addressData = AddressData(
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
            countryCode = CountryCode.US
        )

        val tokenDetails = TokenDetailsData(
            brand = "VISA",
            maskedNumber = "**** **** **** 1234",
            type = "credit",
            expiryDate = "12/23"
        )

        val sessionData = KlarnaSessionData(
            recurringDescription = "Recurring payment",
            purchaseCountry = "US",
            purchaseCurrency = "USD",
            locale = "en_US",
            orderAmount = 200,
            orderLines = orderLines,
            billingAddress = addressData,
            shippingAddress = addressData,
            tokenDetails = tokenDetails,
            orderTaxAmount = 20
        )

        val dataRequest = KlarnaVaultPaymentInstrumentDataRequest(
            klarnaCustomerToken = "customer-token",
            sessionData = sessionData
        )

        val expectedJson = JSONObject().apply {
            putOpt("klarnaCustomerToken", "customer-token")
            put(
                "sessionData",
                JSONObject().apply {
                    putOpt(
                        "recurringDescription",
                        "Recurring payment"
                    )
                    putOpt(
                        "purchaseCountry",
                        "US"
                    )
                    putOpt(
                        "purchaseCurrency",
                        "USD"
                    )
                    putOpt(
                        "locale",
                        "en_US"
                    )
                    putOpt(
                        "orderAmount",
                        200
                    )
                    put(
                        "orderLines",
                        JSONArray().apply {
                            put(
                                JSONObject().apply {
                                    putOpt(
                                        "type",
                                        "physical"
                                    )
                                    putOpt(
                                        "name",
                                        "Test Item"
                                    )
                                    putOpt(
                                        "quantity",
                                        2
                                    )
                                    putOpt(
                                        "reference",
                                        "REF123"
                                    )
                                    putOpt(
                                        "unit_price",
                                        100
                                    )
                                    putOpt(
                                        "total_amount",
                                        200
                                    )
                                    putOpt(
                                        "total_discount_amount",
                                        10
                                    )
                                }
                            )
                        }
                    )
                    put(
                        "billingAddress",
                        JSONObject().apply {
                            put(
                                "firstName",
                                "John"
                            )
                            put(
                                "lastName",
                                "Doe"
                            )
                            put(
                                "email",
                                "john.doe@example.com"
                            )
                            put(
                                "phoneNumber",
                                "+1234567890"
                            )
                            put(
                                "addressLine1",
                                "123 Main St"
                            )
                            put(
                                "addressLine2",
                                "Apt 1"
                            )
                            put(
                                "addressLine3",
                                "Floor 2"
                            )
                            put(
                                "postalCode",
                                "12345"
                            )
                            put(
                                "city",
                                "New York"
                            )
                            put(
                                "state",
                                "NY"
                            )
                            put(
                                "countryCode",
                                "US"
                            )
                        }
                    )
                    put(
                        "shippingAddress",
                        JSONObject().apply {
                            put(
                                "firstName",
                                "John"
                            )
                            put(
                                "lastName",
                                "Doe"
                            )
                            put(
                                "email",
                                "john.doe@example.com"
                            )
                            put(
                                "phoneNumber",
                                "+1234567890"
                            )
                            put(
                                "addressLine1",
                                "123 Main St"
                            )
                            put(
                                "addressLine2",
                                "Apt 1"
                            )
                            put(
                                "addressLine3",
                                "Floor 2"
                            )
                            put(
                                "postalCode",
                                "12345"
                            )
                            put(
                                "city",
                                "New York"
                            )
                            put(
                                "state",
                                "NY"
                            )
                            put(
                                "countryCode",
                                "US"
                            )
                        }
                    )
                    put(
                        "tokenDetails",
                        JSONObject().apply {
                            putOpt(
                                "brand",
                                "VISA"
                            )
                            putOpt(
                                "masked_number",
                                "**** **** **** 1234"
                            )
                            put(
                                "type",
                                "credit"
                            )
                            putOpt(
                                "expiry_date",
                                "12/23"
                            )
                        }
                    )
                    putOpt(
                        "orderTaxAmount",
                        20
                    )
                }
            )
        }

        // When
        val actualJson = KlarnaVaultPaymentInstrumentDataRequest.serializer.serialize(dataRequest)

        // Then
        assertEquals(expectedJson.toString(), actualJson.toString())
    }
}

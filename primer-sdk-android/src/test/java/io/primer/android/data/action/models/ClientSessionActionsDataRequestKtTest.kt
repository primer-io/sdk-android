package io.primer.android.data.action.models

import io.primer.android.core.serialization.json.JSONSerializationUtils.serialize
import io.primer.android.data.tokenization.models.BinData
import io.primer.android.threeds.data.models.auth.Address
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClientSessionActionsDataRequestKtTest {

    @Test
    fun `SetEmailAddress should serialize correctly`() {
        val action = ClientSessionActionsDataRequest.SetEmailAddress("john@doe.com")
        val expectedJson = JSONObject().apply {
            put("type", "SET_EMAIL_ADDRESS")
            put(
                "params",
                JSONObject().apply {
                    put("emailAddress", "john@doe.com")
                }
            )
        }
        assertEquals(expectedJson.toString(), action.serialize().toString())
    }

    @Test
    fun `SetCustomerFirstName should serialize correctly`() {
        val action = ClientSessionActionsDataRequest.SetCustomerFirstName("John")
        val expectedJson = JSONObject().apply {
            put("type", "SET_CUSTOMER_FIRST_NAME")
            put(
                "params",
                JSONObject().apply {
                    put("firstName", "John")
                }
            )
        }
        assertEquals(expectedJson.toString(), action.serialize().toString())
    }

    @Test
    fun `SetCustomerLastName should serialize correctly`() {
        val action = ClientSessionActionsDataRequest.SetCustomerLastName("Doe")
        val expectedJson = JSONObject().apply {
            put("type", "SET_CUSTOMER_LAST_NAME")
            put(
                "params",
                JSONObject().apply {
                    put("lastName", "Doe")
                }
            )
        }
        assertEquals(expectedJson.toString(), action.serialize().toString())
    }

    @Test
    fun `SetPaymentMethod should serialize correctly when bin data is empty`() {
        val action = ClientSessionActionsDataRequest.SetPaymentMethod("VISA", BinData())
        val expectedJson = JSONObject().apply {
            put("type", "SELECT_PAYMENT_METHOD")
            put(
                "params",
                JSONObject().apply {
                    put("paymentMethodType", "VISA")
                    put("binData", JSONObject())
                }
            )
        }
        assertEquals(expectedJson.toString(), action.serialize().toString())
    }

    @Test
    fun `SetPaymentMethod should serialize correctly`() {
        val action = ClientSessionActionsDataRequest.SetPaymentMethod("VISA")
        val expectedJson = JSONObject().apply {
            put("type", "SELECT_PAYMENT_METHOD")
            put(
                "params",
                JSONObject().apply {
                    put("paymentMethodType", "VISA")
                }
            )
        }
        assertEquals(expectedJson.toString(), action.serialize().toString())
    }

    @Test
    fun `UnsetPaymentMethod should serialize correctly`() {
        val action = ClientSessionActionsDataRequest.UnsetPaymentMethod
        val expectedJson = JSONObject().apply {
            put("type", "UNSELECT_PAYMENT_METHOD")
            put("params", JSONObject())
        }
        assertEquals(expectedJson.toString(), action.serialize().toString())
    }

    @Test
    fun `SetBillingAddress should serialize correctly`() {
        val address = Address(
            firstName = "John",
            lastName = "Doe",
            addressLine1 = "123 Main St",
            addressLine2 = "Apt 4B",
            postalCode = "12345",
            city = "Anytown",
            countryCode = "US"
        )
        val action = ClientSessionActionsDataRequest.SetBillingAddress(address)
        val expectedJson = JSONObject().apply {
            put("type", "SET_BILLING_ADDRESS")
            put(
                "params",
                JSONObject().apply {
                    put("billingAddress", address.serialize())
                }
            )
        }
        assertEquals(expectedJson.toString(), action.serialize().toString())
    }

    @Test
    fun `SetShippingMethodId should serialize correctly`() {
        val action = ClientSessionActionsDataRequest.SetShippingMethodId("EXPRESS")
        val expectedJson = JSONObject().apply {
            put("type", "SELECT_SHIPPING_METHOD")
            put(
                "params",
                JSONObject().apply {
                    put("shipping_method_id", "EXPRESS")
                }
            )
        }
        assertEquals(expectedJson.toString(), action.serialize().toString())
    }

    @Test
    fun `SetShippingAddress should serialize correctly`() {
        val address = Address(
            firstName = "John",
            lastName = "Doe",
            addressLine1 = "123 Main St",
            addressLine2 = "Apt 4B",
            postalCode = "12345",
            city = "Anytown",
            countryCode = "US"
        )
        val action = ClientSessionActionsDataRequest.SetShippingAddress(address)
        val expectedJson = JSONObject().apply {
            put("type", "SET_SHIPPING_ADDRESS")
            put(
                "params",
                JSONObject().apply {
                    put("shippingAddress", address.serialize())
                }
            )
        }
        assertEquals(expectedJson.toString(), action.serialize().toString())
    }

    @Test
    fun `SetMobileNumber should serialize correctly`() {
        val action = ClientSessionActionsDataRequest.SetMobileNumber("1234567890")
        val expectedJson = JSONObject().apply {
            put("type", "SET_MOBILE_NUMBER")
            put(
                "params",
                JSONObject().apply {
                    put("mobileNumber", "1234567890")
                }
            )
        }
        assertEquals(expectedJson.toString(), action.serialize().toString())
    }

    @Test
    fun `ClientSessionActionsDataRequest should serialize correctly`() {
        val actions = listOf(
            ClientSessionActionsDataRequest.SetEmailAddress("john@doe.com"),
            ClientSessionActionsDataRequest.SetCustomerFirstName("John"),
            ClientSessionActionsDataRequest.SetCustomerLastName("Doe"),
            ClientSessionActionsDataRequest.SetPaymentMethod("VISA"),
            ClientSessionActionsDataRequest.UnsetPaymentMethod,
            ClientSessionActionsDataRequest.SetBillingAddress(
                Address(
                    firstName = "John",
                    lastName = "Doe",
                    addressLine1 = "123 Main St",
                    addressLine2 = "Apt 4B",
                    postalCode = "12345",
                    city = "Anytown",
                    countryCode = "US"
                )
            ),
            ClientSessionActionsDataRequest.SetShippingMethodId("EXPRESS")
        )
        val request = ClientSessionActionsDataRequest(actions)
        val expectedJson = JSONObject().apply {
            put(
                "actions",
                JSONArray().apply {
                    put(
                        JSONObject().apply {
                            put("type", "SET_EMAIL_ADDRESS")
                            put(
                                "params",
                                JSONObject().apply {
                                    put("emailAddress", "john@doe.com")
                                }
                            )
                        }
                    )
                    put(
                        JSONObject().apply {
                            put("type", "SET_CUSTOMER_FIRST_NAME")
                            put(
                                "params",
                                JSONObject().apply {
                                    put("firstName", "John")
                                }
                            )
                        }
                    )
                    put(
                        JSONObject().apply {
                            put("type", "SET_CUSTOMER_LAST_NAME")
                            put(
                                "params",
                                JSONObject().apply {
                                    put("lastName", "Doe")
                                }
                            )
                        }
                    )
                    put(
                        JSONObject().apply {
                            put("type", "SELECT_PAYMENT_METHOD")
                            put(
                                "params",
                                JSONObject().apply {
                                    put("paymentMethodType", "VISA")
                                }
                            )
                        }
                    )
                    put(
                        JSONObject().apply {
                            put("type", "UNSELECT_PAYMENT_METHOD")
                            put("params", JSONObject())
                        }
                    )
                    put(
                        JSONObject().apply {
                            put("type", "SET_BILLING_ADDRESS")
                            put(
                                "params",
                                JSONObject().apply {
                                    put(
                                        "billingAddress",
                                        JSONObject().apply {
                                            put("firstName", "John")
                                            put("lastName", "Doe")
                                            put("addressLine1", "123 Main St")
                                            put("addressLine2", "Apt 4B")
                                            put("postalCode", "12345")
                                            put("city", "Anytown")
                                            put("countryCode", "US")
                                        }
                                    )
                                }
                            )
                        }
                    )
                    put(
                        JSONObject().apply {
                            put("type", "SELECT_SHIPPING_METHOD")
                            put(
                                "params",
                                JSONObject().apply {
                                    put("shipping_method_id", "EXPRESS")
                                }
                            )
                        }
                    )
                }
            )
        }
        assertEquals(expectedJson.toString(), ClientSessionActionsDataRequest.serializer.serialize(request).toString())
    }
}

package io.primer.android.clientSessionActions.data.models

import io.primer.android.clientSessionActions.data.models.ClientSessionActionsDataRequest.Action.Companion.PARAMS_FIELD
import io.primer.android.clientSessionActions.data.models.ClientSessionActionsDataRequest.Action.Companion.TYPE_FIELD
import io.primer.android.clientSessionActions.domain.models.ActionUpdateBillingAddressParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateSelectPaymentMethodParams
import io.primer.android.clientSessionActions.domain.models.ActionUpdateUnselectPaymentMethodParams
import io.primer.android.configuration.data.model.AddressData
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.core.data.model.EmptyDataRequest
import io.primer.android.core.data.serialization.json.JSONSerializationUtils.serialize
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import kotlin.test.assertIs

internal class ClientSessionActionsDataRequestTest {
    @Test
    fun `serializer correctly serializes SetPaymentMethod to JSON`() {
        // Create a SetPaymentMethod object using the params
        val setPaymentMethod =
            ClientSessionActionsDataRequest.SetPaymentMethod(
                paymentMethodType = "credit_card",
                binData = BinData("Visa"),
            )

        // Use the serializer to convert the SetPaymentMethod object to a JSONObject
        val jsonObject = ClientSessionActionsDataRequest.SetPaymentMethod.serializer.serialize(setPaymentMethod)

        // Expected JSON structure
        val expectedJson =
            JSONObject().apply {
                put(
                    "type",
                    "SELECT_PAYMENT_METHOD",
                )
                put(
                    "params",
                    JSONObject().apply {
                        put(
                            "paymentMethodType",
                            "credit_card",
                        )
                        put(
                            "binData",
                            JSONObject().apply {
                                put(
                                    "network",
                                    "Visa",
                                )
                            },
                        )
                    },
                )
            }

        // Assert that the generated JSON matches the expected JSON
        assertEquals(expectedJson.toString(), jsonObject.toString())
    }

    @Test
    fun `serializer correctly serializes SetPaymentMethod to JSON without binData`() {
        val params =
            ClientSessionActionsDataRequest.SetPaymentMethod(
                paymentMethodType = "credit_card",
            )

        // Use the serializer to convert the SetPaymentMethod object to a JSONObject
        val jsonObject = ClientSessionActionsDataRequest.SetPaymentMethod.serializer.serialize(params)

        // Expected JSON structure
        val expectedJson =
            JSONObject().apply {
                put(
                    "type",
                    "SELECT_PAYMENT_METHOD",
                )
                put(
                    "params",
                    JSONObject().apply {
                        put(
                            "paymentMethodType",
                            "credit_card",
                        )
                    },
                )
            }

        // Assert that the generated JSON matches the expected JSON
        assertEquals(expectedJson.toString(), jsonObject.toString())
    }

    @Test
    fun `serializer correctly serializes UnsetPaymentMethod to JSON`() {
        val action = ClientSessionActionsDataRequest.UnsetPaymentMethod

        val json = ClientSessionActionsDataRequest.UnsetPaymentMethod.serializer.serialize(action)

        assertEquals("UNSELECT_PAYMENT_METHOD", json.getString("type"))
        val paramsJson = json.getJSONObject("params")
        assertEquals(EmptyDataRequest.serializer.serialize(EmptyDataRequest()).toString(), paramsJson.toString())
    }

    @Test
    fun `serializer correctly serializes SetBillingAddress to JSON`() {
        // Create test data
        val addressData = AddressData(city = "123 Main St", addressLine1 = "Springfield", postalCode = "12345")
        val setBillingAddressAction = ClientSessionActionsDataRequest.SetBillingAddress(addressData)

        // Serialize the action object
        val json = ClientSessionActionsDataRequest.SetBillingAddress.serializer.serialize(setBillingAddressAction)

        // Verify serialization
        assertEquals("SET_BILLING_ADDRESS", json.getString(TYPE_FIELD))
        assertEquals(
            addressData.city,
            json.getJSONObject(PARAMS_FIELD).getJSONObject("billingAddress").getString("city"),
        )
        assertEquals(
            addressData.addressLine1,
            json.getJSONObject(PARAMS_FIELD).getJSONObject("billingAddress").getString("addressLine1"),
        )
        assertEquals(
            addressData.postalCode,
            json.getJSONObject(PARAMS_FIELD).getJSONObject("billingAddress").getString("postalCode"),
        )
    }

    @Test
    fun `test ClientSessionActionsDataRequest serializer with SetPaymentMethod`() {
        // Arrange
        val paymentMethodType = "CreditCard"
        val binData = BinData(network = "VISA")
        val action =
            ClientSessionActionsDataRequest.SetPaymentMethod(
                paymentMethodType = paymentMethodType,
                binData = binData,
            )
        val request = ClientSessionActionsDataRequest(actions = listOf(action))

        // Act
        val json = ClientSessionActionsDataRequest.serializer.serialize(request)
        val actionsArray = json.getJSONArray("actions")
        val actionJson = actionsArray.getJSONObject(0)
        val paramsJson = actionJson.getJSONObject("params")

        // Assert
        assertEquals("SELECT_PAYMENT_METHOD", actionJson.getString("type"))
        assertEquals(paymentMethodType, paramsJson.getString("paymentMethodType"))
        assertEquals(binData.network, paramsJson.getJSONObject("binData").getString("network"))
    }

    @Test
    fun `test ClientSessionActionsDataRequest serializer with UnsetPaymentMethod`() {
        // Arrange
        val action = ClientSessionActionsDataRequest.UnsetPaymentMethod
        val request = ClientSessionActionsDataRequest(actions = listOf(action))

        // Act
        val json = ClientSessionActionsDataRequest.serializer.serialize(request)
        val actionsArray = json.getJSONArray("actions")
        val actionJson = actionsArray.getJSONObject(0)
        val paramsJson = actionJson.getJSONObject("params")

        // Assert
        assertEquals("UNSELECT_PAYMENT_METHOD", actionJson.getString("type"))
        assertNotNull(paramsJson)
    }

    @Test
    fun `test ClientSessionActionsDataRequest serializer with SetBillingAddress`() {
        // Arrange
        val billingAddress =
            AddressData(
                firstName = "John",
                lastName = "Doe",
                addressLine1 = "123 Main St",
                addressLine2 = "Apt 4B",
                postalCode = "12345",
                city = "Anytown",
                state = "Anystate",
                countryCode = CountryCode.US,
            )
        val action =
            ClientSessionActionsDataRequest.SetBillingAddress(
                address = billingAddress,
            )
        val request = ClientSessionActionsDataRequest(actions = listOf(action))

        // Act
        val json = ClientSessionActionsDataRequest.serializer.serialize(request)
        val actionsArray = json.getJSONArray("actions")
        val actionJson = actionsArray.getJSONObject(0)
        val paramsJson = actionJson.getJSONObject("params")
        val billingAddressJson = paramsJson.getJSONObject("billingAddress")

        // Assert
        assertEquals("SET_BILLING_ADDRESS", actionJson.getString("type"))
        assertEquals(billingAddress.firstName, billingAddressJson.getString("firstName"))
        assertEquals(billingAddress.lastName, billingAddressJson.getString("lastName"))
        assertEquals(billingAddress.addressLine1, billingAddressJson.getString("addressLine1"))
        assertEquals(billingAddress.addressLine2, billingAddressJson.getString("addressLine2"))
        assertEquals(billingAddress.postalCode, billingAddressJson.getString("postalCode"))
        assertEquals(billingAddress.city, billingAddressJson.getString("city"))
        assertEquals(billingAddress.state, billingAddressJson.getString("state"))
        assertEquals(billingAddress.countryCode, billingAddressJson.get("countryCode"))
    }

    @Test
    fun `test ClientSessionActionsDataRequest serializer with SetEmailAddress`() {
        val action = ClientSessionActionsDataRequest.SetEmailAddress("john@doe.com")
        val expectedJson =
            JSONObject().apply {
                put("type", "SET_EMAIL_ADDRESS")
                put(
                    "params",
                    JSONObject().apply {
                        put("emailAddress", "john@doe.com")
                    },
                )
            }
        assertEquals(expectedJson.toString(), action.serialize().toString())
    }

    @Test
    fun `test ClientSessionActionsDataRequest serializer with SetCustomerFirstName`() {
        val action = ClientSessionActionsDataRequest.SetCustomerFirstName("John")
        val expectedJson =
            JSONObject().apply {
                put("type", "SET_CUSTOMER_FIRST_NAME")
                put(
                    "params",
                    JSONObject().apply {
                        put("firstName", "John")
                    },
                )
            }
        assertEquals(expectedJson.toString(), action.serialize().toString())
    }

    @Test
    fun `test ClientSessionActionsDataRequest serializer with SetCustomerLastName`() {
        val action = ClientSessionActionsDataRequest.SetCustomerLastName("Doe")
        val expectedJson =
            JSONObject().apply {
                put("type", "SET_CUSTOMER_LAST_NAME")
                put(
                    "params",
                    JSONObject().apply {
                        put("lastName", "Doe")
                    },
                )
            }
        assertEquals(expectedJson.toString(), action.serialize().toString())
    }

    @Test
    fun `test ClientSessionActionsDataRequest serializer with SetShippingMethodId`() {
        val action = ClientSessionActionsDataRequest.SetShippingMethodId("EXPRESS")
        val expectedJson =
            JSONObject().apply {
                put("type", "SELECT_SHIPPING_METHOD")
                put(
                    "params",
                    JSONObject().apply {
                        put("shipping_method_id", "EXPRESS")
                    },
                )
            }
        assertEquals(expectedJson.toString(), action.serialize().toString())
    }

    @Test
    fun `test ClientSessionActionsDataRequest serializer with SetShippingAddress`() {
        val address =
            AddressData(
                firstName = "John",
                lastName = "Doe",
                addressLine1 = "123 Main St",
                addressLine2 = "Apt 4B",
                postalCode = "12345",
                city = "Anytown",
                countryCode = CountryCode.US,
            )
        val action = ClientSessionActionsDataRequest.SetShippingAddress(address)
        val expectedJson =
            JSONObject().apply {
                put("type", "SET_SHIPPING_ADDRESS")
                put(
                    "params",
                    JSONObject().apply {
                        put("shippingAddress", address.serialize())
                    },
                )
            }
        assertEquals(expectedJson.toString(), action.serialize().toString())
    }

    @Test
    fun `test ClientSessionActionsDataRequest serializer with SetMobileNumber`() {
        val action = ClientSessionActionsDataRequest.SetMobileNumber("1234567890")
        val expectedJson =
            JSONObject().apply {
                put("type", "SET_MOBILE_NUMBER")
                put(
                    "params",
                    JSONObject().apply {
                        put("mobileNumber", "1234567890")
                    },
                )
            }
        assertEquals(expectedJson.toString(), action.serialize().toString())
    }
}

internal class BaseActionUpdateParamsTest {
    @Test
    fun `test toActionData with ActionUpdateSelectPaymentMethodParams`() {
        // Arrange
        val paymentMethodType = "CreditCard"
        val cardNetwork = "VISA"
        val params =
            ActionUpdateSelectPaymentMethodParams(
                paymentMethodType = paymentMethodType,
                cardNetwork = cardNetwork,
            )

        // Act
        val action = params.toActionData()

        // Assert
        assertIs<List<ClientSessionActionsDataRequest.SetPaymentMethod>>(action)
        val requestDataParams = action.single()
        assertEquals(paymentMethodType, requestDataParams.paymentMethodType)
        assertEquals(cardNetwork, requestDataParams.binData?.network)
    }

    @Test
    fun `test toActionData with ActionUpdateUnselectPaymentMethodParams`() {
        // Arrange
        val params = ActionUpdateUnselectPaymentMethodParams

        // Act
        val action = params.toActionData()

        // Assert
        assertIs<List<ClientSessionActionsDataRequest.UnsetPaymentMethod>>(action)
    }

    @Test
    fun `test toActionData with ActionUpdateBillingAddressParams`() {
        // Arrange
        val firstName = "John"
        val lastName = "Doe"
        val addressLine1 = "123 Main St"
        val addressLine2 = "Apt 4B"
        val postalCode = "12345"
        val city = "Any town"
        val state = "Any state"
        val countryCode = "US"
        val params =
            ActionUpdateBillingAddressParams(
                firstName = firstName,
                lastName = lastName,
                addressLine1 = addressLine1,
                addressLine2 = addressLine2,
                postalCode = postalCode,
                city = city,
                state = state,
                countryCode = countryCode,
            )

        // Act
        val action = params.toActionData()

        // Assert
        assertIs<List<ClientSessionActionsDataRequest.SetBillingAddress>>(action)
        val requestDataParams = action.single()
        val billingAddress = requestDataParams.address
        assertEquals(firstName, billingAddress.firstName)
        assertEquals(lastName, billingAddress.lastName)
        assertEquals(addressLine1, billingAddress.addressLine1)
        assertEquals(addressLine2, billingAddress.addressLine2)
        assertEquals(postalCode, billingAddress.postalCode)
        assertEquals(city, billingAddress.city)
        assertEquals(state, billingAddress.state)
        assertEquals(CountryCode.US, billingAddress.countryCode)
    }
}

internal class BinDataTest {
    @Test
    fun `test BinData serialization with non-null network`() {
        // Arrange
        val network = "VISA"
        val binData = BinData(network)

        // Act
        val json = BinData.serializer.serialize(binData)

        // Assert
        assertEquals(network, json.getString("network"))
    }

    @Test
    fun `test BinData serialization with null network`() {
        // Arrange
        val binData = BinData(null)

        // Act
        val json = BinData.serializer.serialize(binData)

        // Assert
        assertEquals(JSONObject.NULL, json.opt("network"))
    }

    @Test
    fun `test BinData deserialization with non-null network`() {
        // Arrange
        val network = "MASTERCARD"
        val json =
            JSONObject().apply {
                put("network", network)
            }

        // Act
        val binData = BinData.deserializer.deserialize(json)

        // Assert
        assertEquals(network, binData.network)
    }

    @Test
    fun `test BinData deserialization with null network`() {
        // Arrange
        val json =
            JSONObject().apply {
                put("network", JSONObject.NULL)
            }

        // Act
        val binData = BinData.deserializer.deserialize(json)

        // Assert
        assertEquals(null, binData.network)
    }
}

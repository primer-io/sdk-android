package io.primer.android.klarna.implementation.session.data.models

import io.mockk.mockkStatic
import io.primer.android.configuration.data.model.AddressData
import io.primer.android.configuration.data.model.CountryCode
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import org.json.JSONArray
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CreateCheckoutPaymentSessionDataRequestTest {
    @BeforeEach
    fun setUp() {
        mockkStatic(JSONSerializationUtils::class)
    }

    @Test
    fun `OrderItem serializer should correctly serialize OrderItem`() {
        // Given
        val orderItem =
            CreateCheckoutPaymentSessionDataRequest.OrderItem(
                name = "Test Item",
                unitAmount = 100,
                reference = "REF123",
                quantity = 2,
                discountAmount = 10,
                productType = "Test Product",
                taxAmount = 5,
            )
        val expectedJson =
            JSONObject().apply {
                put("name", "Test Item")
                put("unitAmount", 100)
                put("reference", "REF123")
                put("quantity", 2)
                put("discountAmount", 10)
                put("productType", "Test Product")
                put("taxAmount", 5)
            }

        // When
        val actualJson = CreateCheckoutPaymentSessionDataRequest.OrderItem.serializer.serialize(orderItem)

        // Then
        assertEquals(expectedJson.toString(), actualJson.toString())
    }

    @Test
    fun `CreateCheckoutPaymentSessionDataRequest serializer should correctly serialize request`() {
        // Given
        val items =
            listOf(
                CreateCheckoutPaymentSessionDataRequest.OrderItem(
                    name = "Test Item",
                    unitAmount = 100,
                    reference = "REF123",
                    quantity = 2,
                    discountAmount = 10,
                    productType = "Test Product",
                    taxAmount = 5,
                ),
            )

        val request =
            CreateCheckoutPaymentSessionDataRequest(
                paymentMethodConfigId = "config-id",
                sessionType = KlarnaSessionType.ONE_OFF_PAYMENT,
                totalAmount = 200,
                localeData =
                LocaleDataRequest(
                    countryCode = CountryCode.US,
                    currencyCode = "USD",
                    localeCode = "en_US",
                ),
                orderItems = items,
                billingAddress =
                AddressData(
                    firstName = "firstName",
                    lastName = "lastName",
                ),
                shippingAddress =
                AddressData(
                    firstName = "firstName",
                    lastName = "lastName",
                ),
            )

        val localeDataJson =
            JSONObject().apply {
                put("countryCode", "US")
                put("currencyCode", "USD")
                put("localeCode", "en_US")
            }

        val billingAddressJson =
            JSONObject().apply {
                put("firstName", "firstName")
                put("lastName", "lastName")
            }

        val shippingAddressJson =
            JSONObject().apply {
                put("firstName", "firstName")
                put("lastName", "lastName")
            }

        val expectedJson =
            JSONObject().apply {
                put("paymentMethodConfigId", "config-id")
                put("sessionType", KlarnaSessionType.ONE_OFF_PAYMENT.name)
                put("totalAmount", 200)
                put("localeData", localeDataJson)
                put(
                    "orderItems",
                    JSONArray().put(CreateCheckoutPaymentSessionDataRequest.OrderItem.serializer.serialize(items[0])),
                )
                put("billingAddress", billingAddressJson)
                put("shippingAddress", shippingAddressJson)
            }

        // When
        val actualJson = CreateCheckoutPaymentSessionDataRequest.serializer.serialize(request)

        // Then
        assertEquals(expectedJson.toString(), actualJson.toString())
    }
}

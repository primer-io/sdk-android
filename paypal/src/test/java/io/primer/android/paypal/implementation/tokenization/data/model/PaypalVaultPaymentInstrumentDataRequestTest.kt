package io.primer.android.paypal.implementation.tokenization.data.model

import io.mockk.mockkStatic
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PaypalVaultPaymentInstrumentDataRequestTest {
    @BeforeEach
    fun setUp() {
        mockkStatic(JSONSerializationUtils::class)
    }

    @Test
    fun `PaypalVaultPaymentInstrumentDataRequest should serialize correctly`() {
        // Arrange
        val billingAgreementId = "agreement123"
        val externalPayerInfo =
            PaypalExternalPayerInfo(
                email = "email@example.com",
                externalPayerId = "payer123",
                firstName = "John",
                lastName = "Doe",
            )
        val shippingAddress =
            PaypalShippingAddressDataResponse(
                firstName = "John",
                lastName = "Doe",
                addressLine1 = "123 Main St",
                city = "San Francisco",
                countryCode = "US",
                postalCode = "94103",
                state = "CA",
                addressLine2 = null,
            )
        val request =
            PaypalPaymentInstrumentDataRequest.PaypalVaultPaymentInstrumentDataRequest(
                billingAgreementId = billingAgreementId,
                externalPayerInfo = externalPayerInfo,
                shippingAddress = shippingAddress,
            )

        // Act
        val json =
            PaypalPaymentInstrumentDataRequest.PaypalVaultPaymentInstrumentDataRequest.serializer.serialize(request)

        // Assert
        val expectedJson =
            JSONObject().apply {
                put("paypalBillingAgreementId", billingAgreementId)
                put(
                    "externalPayerInfo",
                    JSONObject()
                        .apply {
                            put("email", externalPayerInfo.email)
                            put("externalPayerId", externalPayerInfo.externalPayerId)
                            put("firstName", externalPayerInfo.firstName)
                            put("lastName", externalPayerInfo.lastName)
                        },
                )
                put(
                    "shippingAddress",
                    JSONObject()
                        .apply {
                            put("firstName", shippingAddress.firstName)
                            put("lastName", shippingAddress.lastName)
                            put("addressLine1", shippingAddress.addressLine1)
                            put("city", shippingAddress.city)
                            put("countryCode", shippingAddress.countryCode)
                            put("postalCode", shippingAddress.postalCode)
                            put("state", shippingAddress.state)
                        },
                )
            }
        assertEquals(expectedJson.toString(), json.toString())
    }
}

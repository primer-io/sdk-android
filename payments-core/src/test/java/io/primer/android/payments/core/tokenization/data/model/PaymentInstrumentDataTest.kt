package io.primer.android.payments.core.tokenization.data.model

import io.primer.android.data.tokenization.models.PaymentInstrumentData
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaymentInstrumentDataTest {
    @Test
    fun `test deserialization of PaymentInstrumentData`() {
        // Given
        val json =
            JSONObject().apply {
                put(
                    "network",
                    "Visa",
                )
                put(
                    "cardholderName",
                    "John Doe",
                )
                put(
                    "first6Digits",
                    123456,
                )
                put(
                    "last4Digits",
                    7890,
                )
                put(
                    "expirationMonth",
                    12,
                )
                put(
                    "expirationYear",
                    2025,
                )
                put(
                    "gocardlessMandateId",
                    "gocardless123",
                )
                put(
                    "klarnaCustomerToken",
                    "klarnaToken123",
                )
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
                put(
                    "externalPayerInfo",
                    JSONObject().apply {
                        put(
                            "email",
                            "john.doe@example.com",
                        )
                        put(
                            "externalPayerId",
                            "external123",
                        )
                        put(
                            "firstName",
                            "John",
                        )
                        put(
                            "lastName",
                            "Doe",
                        )
                    },
                )
                put(
                    "sessionData",
                    JSONObject().apply {
                        put(
                            "recurringDescription",
                            "Monthly subscription",
                        )
                        put(
                            "billingAddress",
                            JSONObject().apply {
                                put(
                                    "email",
                                    "billing@example.com",
                                )
                            },
                        )
                    },
                )
            }

        // When
        val paymentInstrumentData = PaymentInstrumentData.deserializer.deserialize(json)

        // Then
        assertEquals("Visa", paymentInstrumentData.network)
        assertEquals("John Doe", paymentInstrumentData.cardholderName)
        assertEquals(123456, paymentInstrumentData.first6Digits)
        assertEquals(7890, paymentInstrumentData.last4Digits)
        assertEquals(12, paymentInstrumentData.expirationMonth)
        assertEquals(2025, paymentInstrumentData.expirationYear)
        assertEquals("klarnaToken123", paymentInstrumentData.klarnaCustomerToken)
        assertEquals("credit_card", paymentInstrumentData.paymentMethodType)
        assertEquals("Visa", paymentInstrumentData.binData?.network)

        assertEquals("john.doe@example.com", paymentInstrumentData.externalPayerInfo?.email)
        assertEquals("external123", paymentInstrumentData.externalPayerInfo?.externalPayerId)
        assertEquals("John", paymentInstrumentData.externalPayerInfo?.firstName)
        assertEquals("Doe", paymentInstrumentData.externalPayerInfo?.lastName)

        assertEquals("Monthly subscription", paymentInstrumentData.sessionData?.recurringDescription)
        assertEquals("billing@example.com", paymentInstrumentData.sessionData?.billingAddress?.email)
    }
}

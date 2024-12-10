package io.primer.android.payments.core.create.data.model

import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PaymentDataResponseTest {

    @Test
    fun `test PaymentDataResponse deserialization`() {
        // Arrange
        val json = JSONObject().apply {
            put(
                "id",
                "payment123"
            )
            put(
                "date",
                "2024-06-25"
            )
            put(
                "status",
                "SUCCESS"
            )
            put(
                "orderId",
                "order456"
            )
            put(
                "currencyCode",
                "USD"
            )
            put(
                "amount",
                1000
            )
            put(
                "customerId",
                "customer789"
            )
            put(
                "paymentFailureReason",
                "None"
            )
            put(

                "requiredAction",
                JSONObject().apply {
                    put(
                        "name",
                        "3DS_AUTHENTICATION"
                    )
                    put(
                        "description",
                        "Authenticate the transaction"
                    )
                    put(
                        "clientToken",
                        "token123"
                    )
                }
            )
        }

        // Act
        val paymentDataResponse = PaymentDataResponse.deserializer.deserialize(json)

        // Assert
        assertEquals("payment123", paymentDataResponse.id)
        assertEquals("2024-06-25", paymentDataResponse.date)
        assertEquals(PaymentStatus.SUCCESS, paymentDataResponse.status)
        assertEquals("order456", paymentDataResponse.orderId)
        assertEquals("USD", paymentDataResponse.currencyCode)
        assertEquals(1000, paymentDataResponse.amount)
        assertEquals("customer789", paymentDataResponse.customerId)
        assertEquals("None", paymentDataResponse.paymentFailureReason)
        assertEquals(RequiredActionName.`3DS_AUTHENTICATION`, paymentDataResponse.requiredAction?.name)
        assertEquals("Authenticate the transaction", paymentDataResponse.requiredAction?.description)
        assertEquals("token123", paymentDataResponse.requiredAction?.clientToken)
    }

    @Test
    fun `test PaymentDataResponse toPaymentResult conversion`() {
        // Arrange
        val requiredActionData = RequiredActionData(
            name = RequiredActionName.`3DS_AUTHENTICATION`,
            description = "Authenticate the transaction",
            clientToken = "token123"
        )
        val paymentDataResponse = PaymentDataResponse(
            id = "payment123",
            date = "2024-06-25",
            status = PaymentStatus.SUCCESS,
            orderId = "order456",
            currencyCode = "USD",
            amount = 1000,
            customerId = "customer789",
            paymentFailureReason = "None",
            requiredAction = requiredActionData,
            showSuccessCheckoutOnPendingPayment = false
        )

        // Act
        val paymentResult = paymentDataResponse.toPaymentResult()

        // Assert
        assertEquals("payment123", paymentResult.payment.id)
        assertEquals("order456", paymentResult.payment.orderId)
        assertEquals(PaymentStatus.SUCCESS, paymentResult.paymentStatus)
        assertEquals(RequiredActionName.`3DS_AUTHENTICATION`, paymentResult.requiredActionName)
        assertEquals("token123", paymentResult.clientToken)
        assertEquals(null, paymentResult.paymentMethodData)
    }
}

internal class RequiredActionDataTest {

    @Test
    fun `test RequiredActionData deserialization`() {
        // Arrange
        val json = JSONObject().apply {
            put(
                "name",
                "3DS_AUTHENTICATION"
            )
            put(
                "description",
                "Authenticate the transaction"
            )
            put(
                "clientToken",
                "token123"
            )
        }

        // Act
        val requiredActionData = RequiredActionData.deserializer.deserialize(json)

        // Assert
        assertEquals(RequiredActionName.`3DS_AUTHENTICATION`, requiredActionData.name)
        assertEquals("Authenticate the transaction", requiredActionData.description)
        assertEquals("token123", requiredActionData.clientToken)
    }
}

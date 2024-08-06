package io.primer.android.data.payments.create.models

import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.domain.payments.create.model.PaymentResult
import org.json.JSONObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaymentDataResponseTest {

    @Test
    fun `deserializer should correctly parse JSON into PaymentDataResponse`() {
        val jsonString = """
            {
                "id": "payment123",
                "date": "2023-10-01",
                "status": "PENDING",
                "orderId": "order456",
                "currencyCode": "USD",
                "amount": 1000,
                "customerId": "customer789",
                "paymentFailureReason": "Insufficient funds",
                "requiredAction": {
                    "name": "3DS_AUTHENTICATION",
                    "description": "3DS authentication required",
                    "clientToken": "token123"
                },
                "showSuccessCheckoutOnPendingPayment": true
            }
        """.trimIndent()

        val jsonObject = JSONObject(jsonString)
        val paymentDataResponse = JSONSerializationUtils.getJsonObjectDeserializer<PaymentDataResponse>()
            .deserialize(jsonObject)

        val expectedPaymentDataResponse = PaymentDataResponse(
            id = "payment123",
            date = "2023-10-01",
            status = PaymentStatus.PENDING,
            orderId = "order456",
            currencyCode = "USD",
            amount = 1000,
            customerId = "customer789",
            paymentFailureReason = "Insufficient funds",
            requiredAction = RequiredActionData(
                name = RequiredActionName.`3DS_AUTHENTICATION`,
                description = "3DS authentication required",
                clientToken = "token123"
            ),
            showSuccessCheckoutOnPendingPayment = true
        )

        assertEquals(expectedPaymentDataResponse, paymentDataResponse)
    }

    @Test
    fun `toPaymentResult should correctly convert PaymentDataResponse to PaymentResult`() {
        val paymentDataResponse = PaymentDataResponse(
            id = "payment123",
            date = "2023-10-01",
            status = PaymentStatus.PENDING,
            orderId = "order456",
            currencyCode = "USD",
            amount = 1000,
            customerId = "customer789",
            paymentFailureReason = "Insufficient funds",
            requiredAction = RequiredActionData(
                name = RequiredActionName.`3DS_AUTHENTICATION`,
                description = "3DS authentication required",
                clientToken = "token123"
            ),
            showSuccessCheckoutOnPendingPayment = true
        )

        val expectedPaymentResult = PaymentResult(
            payment = Payment(
                id = "payment123",
                orderId = "order456"
            ),
            paymentStatus = PaymentStatus.PENDING,
            requiredActionName = RequiredActionName.`3DS_AUTHENTICATION`,
            clientToken = "token123",
            paymentMethodData = null,
            showSuccessCheckoutOnPendingPayment = true
        )

        val actualPaymentResult = paymentDataResponse.toPaymentResult()

        assertEquals(expectedPaymentResult, actualPaymentResult)
    }
}

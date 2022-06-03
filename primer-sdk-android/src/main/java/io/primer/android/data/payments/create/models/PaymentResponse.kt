package io.primer.android.data.payments.create.models

import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.domain.payments.create.model.PaymentResult
import kotlinx.serialization.Serializable

@Serializable
internal data class PaymentResponse(
    val id: String,
    val date: String,
    val status: PaymentStatus,
    val orderId: String,
    val currencyCode: String,
    val amount: Int,
    val customerId: String? = null,
    val paymentFailureReason: String? = null,
    val requiredAction: RequiredActionData? = null,
)

@Serializable
internal enum class PaymentStatus {

    PENDING,
    SUCCESS,
    FAILED
}

@Serializable
internal data class RequiredActionData(
    val name: RequiredActionName,
    val description: String,
    val clientToken: String? = null,
)

@Serializable
internal enum class RequiredActionName {

    `3DS_AUTHENTICATION`,
    USE_PRIMER_SDK,
    PROCESSOR_3DS
}

internal fun PaymentResponse.toPaymentResult() = PaymentResult(
    Payment(
        id,
        orderId
    ),
    status,
    requiredAction?.clientToken
)

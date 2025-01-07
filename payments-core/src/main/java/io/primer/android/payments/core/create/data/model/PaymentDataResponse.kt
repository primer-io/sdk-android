package io.primer.android.payments.core.create.data.model

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import io.primer.android.domain.payments.create.model.Payment
import io.primer.android.payments.core.additionalInfo.PrimerCheckoutAdditionalInfo
import io.primer.android.payments.core.create.domain.model.PaymentResult

data class PaymentDataResponse(
    val id: String,
    val date: String,
    val status: PaymentStatus,
    val orderId: String,
    val currencyCode: String,
    val amount: Int,
    val customerId: String?,
    val paymentFailureReason: String?,
    val requiredAction: RequiredActionData?,
    val showSuccessCheckoutOnPendingPayment: Boolean?,
) : JSONDeserializable {
    companion object {
        private const val ID_FIELD = "id"
        private const val DATE_FIELD = "date"
        private const val PAYMENT_STATUS_FIELD = "status"
        private const val ORDER_ID_FIELD = "orderId"
        private const val CURRENCY_CODE_FIELD = "currencyCode"
        private const val AMOUNT_FIELD = "amount"
        private const val CUSTOMER_ID_FIELD = "customerId"
        private const val PAYMENT_FAILURE_REASON_FIELD = "paymentFailureReason"
        private const val REQUIRED_ACTION_DATA_FIELD = "requiredAction"
        private const val SHOW_SUCCESS_CHECKOUT_ON_PENDING_PAYMENT_FIELD = "showSuccessCheckoutOnPendingPayment"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                PaymentDataResponse(
                    t.getString(ID_FIELD),
                    t.getString(DATE_FIELD),
                    PaymentStatus.valueOf(t.getString(PAYMENT_STATUS_FIELD)),
                    t.getString(ORDER_ID_FIELD),
                    t.getString(CURRENCY_CODE_FIELD),
                    t.getInt(AMOUNT_FIELD),
                    t.optNullableString(CUSTOMER_ID_FIELD),
                    t.optNullableString(PAYMENT_FAILURE_REASON_FIELD),
                    t.optJSONObject(REQUIRED_ACTION_DATA_FIELD)?.let {
                        JSONSerializationUtils.getJsonObjectDeserializer<RequiredActionData>()
                            .deserialize(it)
                    },
                    t.optBoolean(SHOW_SUCCESS_CHECKOUT_ON_PENDING_PAYMENT_FIELD),
                )
            }
    }
}

enum class PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
}

data class RequiredActionData(
    val name: RequiredActionName,
    val description: String,
    val clientToken: String?,
) : JSONDeserializable {
    companion object {
        private const val REQUIRED_ACTION_NAME_FIELD = "name"
        private const val DESCRIPTION_FIELD = "description"
        private const val CLIENT_TOKEN_FIELD = "clientToken"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { t ->
                RequiredActionData(
                    RequiredActionName.valueOf(t.getString(REQUIRED_ACTION_NAME_FIELD)),
                    t.getString(DESCRIPTION_FIELD),
                    t.optNullableString(CLIENT_TOKEN_FIELD),
                )
            }
    }
}

enum class RequiredActionName {
    @Suppress("EnumEntryName", "EnumNaming")
    `3DS_AUTHENTICATION`,
    USE_PRIMER_SDK,
    PROCESSOR_3DS,
    PAYMENT_METHOD_VOUCHER,
}

fun PaymentDataResponse.toPaymentResult(paymentMethodData: PrimerCheckoutAdditionalInfo? = null) =
    PaymentResult(
        Payment(
            id = id,
            orderId = orderId,
        ),
        paymentStatus = status,
        requiredActionName = requiredAction?.name,
        clientToken = requiredAction?.clientToken,
        paymentMethodData = paymentMethodData,
        showSuccessCheckoutOnPendingPayment = showSuccessCheckoutOnPendingPayment == true,
    )

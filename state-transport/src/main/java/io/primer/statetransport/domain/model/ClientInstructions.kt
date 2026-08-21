package io.primer.statetransport.domain.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

sealed interface ClientInstructions {

    data class Execute(
        val pollDelayMilliseconds: Long,
        val payload: String,
    ) : ClientInstructions

    data class End(
        val checkoutOutcome: CheckoutOutcome?,
        val payment: PaymentInfo?,
    ) : ClientInstructions

    data class Wait(
        val pollDelayMilliseconds: Long,
    ) : ClientInstructions
}

data class InstructionFetch(
    val instruction: ClientInstructions,
    val currentAttempt: CurrentAttempt? = null,
)

data class CurrentAttempt(
    val id: String,
    val paymentInstrumentTokenId: String? = null,
    val paymentId: String? = null,
) : JSONObjectSerializable {

    companion object {
        private const val ID_FIELD = "id"
        private const val PAYMENT_INSTRUMENT_TOKEN_ID_FIELD = "paymentInstrumentTokenId"
        private const val PAYMENT_ID_FIELD = "paymentId"

        @JvmField
        val serializer = JSONObjectSerializer<CurrentAttempt> { t ->
            JSONObject().apply {
                put(ID_FIELD, t.id)
                putOpt(PAYMENT_INSTRUMENT_TOKEN_ID_FIELD, t.paymentInstrumentTokenId)
                putOpt(PAYMENT_ID_FIELD, t.paymentId)
            }
        }
    }
}

enum class CheckoutOutcome {
    CHECKOUT_COMPLETE,
    CHECKOUT_FAILURE,
    DETERMINE_FROM_PAYMENT_STATUS,
}

data class PaymentInfo(
    val id: String,
    val date: String,
    val amount: Long,
    val currencyCode: String,
    val status: PaymentStatus,
    val customerId: String?,
    val orderId: String,
)

enum class PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED,
}

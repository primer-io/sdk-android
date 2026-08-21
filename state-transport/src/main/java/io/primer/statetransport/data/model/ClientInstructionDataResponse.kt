package io.primer.statetransport.data.model

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.extensions.optNullableLong
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import io.primer.statetransport.domain.model.CheckoutOutcome
import io.primer.statetransport.domain.model.ClientInstructions
import io.primer.statetransport.domain.model.CurrentAttempt
import io.primer.statetransport.domain.model.InstructionFetch
import io.primer.statetransport.domain.model.PaymentInfo
import io.primer.statetransport.domain.model.PaymentStatus

internal enum class ClientInstructionType {
    WAIT,
    EXECUTE,
    END,
}

internal data class ClientInstructionDataResponse(
    val type: ClientInstructionType,
    val pollDelayMilliseconds: Long,
    val payload: String?,
) : JSONDeserializable {

    companion object {
        private const val TYPE_FIELD = "type"
        private const val POLL_DELAY_FIELD = "pollDelayMilliseconds"
        private const val PAYLOAD_FIELD = "payload"

        @JvmField
        val deserializer = JSONObjectDeserializer { json ->
            ClientInstructionDataResponse(
                type = ClientInstructionType.valueOf(json.getString(TYPE_FIELD)),
                pollDelayMilliseconds = requireNotNull(json.optNullableLong(POLL_DELAY_FIELD, 0L)),
                payload = json.optJSONObject(PAYLOAD_FIELD)?.toString(),
            )
        }
    }
}

internal data class CurrentAttemptDataResponse(
    val id: String,
    val paymentInstrumentTokenId: String?,
    val paymentId: String?,
) : JSONDeserializable {

    companion object {
        private const val ID_FIELD = "id"
        private const val PAYMENT_INSTRUMENT_TOKEN_ID_FIELD = "paymentInstrumentTokenId"
        private const val PAYMENT_ID_FIELD = "paymentId"

        @JvmField
        val deserializer = JSONObjectDeserializer { json ->
            CurrentAttemptDataResponse(
                id = json.getString(ID_FIELD),
                paymentInstrumentTokenId = json.optNullableString(PAYMENT_INSTRUMENT_TOKEN_ID_FIELD),
                paymentId = json.optNullableString(PAYMENT_ID_FIELD),
            )
        }
    }
}

internal data class ClientSessionInstructionResponse(
    val clientInstruction: ClientInstructionDataResponse,
    val currentAttempt: CurrentAttemptDataResponse?,
) : JSONDeserializable {

    companion object {
        private const val CLIENT_INSTRUCTION_FIELD = "clientInstruction"
        private const val CURRENT_ATTEMPT_FIELD = "currentAttempt"

        @JvmField
        val deserializer = JSONObjectDeserializer { json ->
            ClientSessionInstructionResponse(
                clientInstruction = json.getJSONObject(CLIENT_INSTRUCTION_FIELD).let {
                    ClientInstructionDataResponse.deserializer.deserialize(it)
                },
                currentAttempt = json.optJSONObject(CURRENT_ATTEMPT_FIELD)?.let {
                    CurrentAttemptDataResponse.deserializer.deserialize(it)
                },
            )
        }
    }
}

private const val CHECKOUT_OUTCOME_FIELD = "checkoutOutcome"
private const val PAYMENT_FIELD = "payment"
private const val ID_FIELD = "id"
private const val DATE_FIELD = "date"
private const val AMOUNT_FIELD = "amount"
private const val CURRENCY_CODE_FIELD = "currencyCode"
private const val STATUS_FIELD = "status"
private const val CUSTOMER_ID_FIELD = "customerId"
private const val ORDER_ID_FIELD = "orderId"

internal fun ClientInstructionDataResponse?.toInstructions(): ClientInstructions = when {
    this == null || type == ClientInstructionType.WAIT -> ClientInstructions.Wait(
        pollDelayMilliseconds = this?.pollDelayMilliseconds ?: 0L,
    )

    type == ClientInstructionType.EXECUTE -> ClientInstructions.Execute(
        pollDelayMilliseconds = pollDelayMilliseconds,
        payload = requireNotNull(payload),
    )

    type == ClientInstructionType.END -> {
        val payloadJson = org.json.JSONObject(requireNotNull(payload))
        ClientInstructions.End(
            checkoutOutcome = payloadJson.optNullableString(CHECKOUT_OUTCOME_FIELD)?.let {
                CheckoutOutcome.valueOf(it)
            },
            payment = payloadJson.optJSONObject(PAYMENT_FIELD)?.let { json ->
                PaymentInfo(
                    id = json.getString(ID_FIELD),
                    date = json.getString(DATE_FIELD),
                    amount = json.getLong(AMOUNT_FIELD),
                    currencyCode = json.getString(CURRENCY_CODE_FIELD),
                    status = PaymentStatus.valueOf(json.getString(STATUS_FIELD)),
                    customerId = json.optNullableString(CUSTOMER_ID_FIELD),
                    orderId = json.getString(ORDER_ID_FIELD),
                )
            },
        )
    }

    else -> ClientInstructions.Wait(
        pollDelayMilliseconds = pollDelayMilliseconds,
    )
}

internal fun ClientSessionInstructionResponse.toInstructionFetch(): InstructionFetch = InstructionFetch(
    instruction = clientInstruction.toInstructions(),
    currentAttempt = currentAttempt?.let { attempt ->
        CurrentAttempt(
            id = attempt.id,
            paymentInstrumentTokenId = attempt.paymentInstrumentTokenId,
            paymentId = attempt.paymentId,
        )
    },
)

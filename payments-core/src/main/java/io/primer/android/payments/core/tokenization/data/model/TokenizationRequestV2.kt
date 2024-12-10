package io.primer.android.payments.core.tokenization.data.model

import androidx.annotation.VisibleForTesting
import io.primer.android.PrimerSessionIntent
import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.data.tokenization.models.TokenType
import org.json.JSONObject

abstract class TokenizationRequestV2<T : BasePaymentInstrumentDataRequest> :
    JSONObjectSerializable {

    @VisibleForTesting
    abstract val paymentInstrument: T
    protected abstract val paymentInstrumentSerializer: JSONObjectSerializer<T>

    companion object {

        @JvmField
        val serializer = JSONObjectSerializer<TokenizationRequestV2<in BasePaymentInstrumentDataRequest>> { t ->
            when (t) {
                is TokenizationCheckoutRequestV2 ->
                    TokenizationCheckoutRequestV2.serializer.serialize(t)

                is TokenizationVaultRequestV2 ->
                    TokenizationVaultRequestV2.serializer.serialize(t)

                else -> throw IllegalStateException("Unsupported instance of $t")
            }
        }
    }
}

inline fun <reified T : BasePaymentInstrumentDataRequest> T.toTokenizationRequest(
    paymentMethodIntent: PrimerSessionIntent
): TokenizationRequestV2<T> {
    return when (paymentMethodIntent) {
        PrimerSessionIntent.CHECKOUT -> TokenizationCheckoutRequestV2(
            this,
            JSONSerializationUtils.getJsonObjectSerializer<T>()
        )

        PrimerSessionIntent.VAULT -> TokenizationVaultRequestV2(
            this,
            JSONSerializationUtils.getJsonObjectSerializer<T>(),
            TokenType.MULTI_USE.name,
            paymentMethodIntent.name
        )
    }
}

data class TokenizationVaultRequestV2<T : BasePaymentInstrumentDataRequest>(
    override val paymentInstrument: T,
    override val paymentInstrumentSerializer: JSONObjectSerializer<T>,
    private val tokenType: String,
    private val paymentFlow: String
) : TokenizationRequestV2<T>() {

    companion object {
        private const val PAYMENT_INSTRUMENT_FIELD = "paymentInstrument"
        private const val TOKEN_TYPE_FIELD = "tokenType"
        private const val PAYMENT_FLOW_FIELD = "paymentFlow"

        @JvmField
        val serializer = JSONObjectSerializer<TokenizationVaultRequestV2<BasePaymentInstrumentDataRequest>> { t ->
            JSONObject().apply {
                put(
                    PAYMENT_INSTRUMENT_FIELD,
                    t.paymentInstrumentSerializer.serialize(t.paymentInstrument)
                )
                put(TOKEN_TYPE_FIELD, t.tokenType)
                put(PAYMENT_FLOW_FIELD, t.paymentFlow)
            }
        }
    }
}

data class TokenizationCheckoutRequestV2<T : BasePaymentInstrumentDataRequest>(
    override val paymentInstrument: T,
    override val paymentInstrumentSerializer: JSONObjectSerializer<T>
) : TokenizationRequestV2<T>() {
    companion object {
        private const val PAYMENT_INSTRUMENT_FIELD = "paymentInstrument"

        @JvmField
        val serializer = JSONObjectSerializer<TokenizationCheckoutRequestV2<BasePaymentInstrumentDataRequest>> { t ->
            JSONObject().apply {
                put(
                    PAYMENT_INSTRUMENT_FIELD,
                    t.paymentInstrumentSerializer.serialize(t.paymentInstrument)
                )
            }
        }
    }
}

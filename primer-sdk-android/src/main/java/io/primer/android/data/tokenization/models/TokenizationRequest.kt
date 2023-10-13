package io.primer.android.data.tokenization.models

import io.primer.android.PrimerSessionIntent
import io.primer.android.core.serialization.json.JSONObjectSerializable
import io.primer.android.core.serialization.json.JSONObjectSerializer
import io.primer.android.domain.tokenization.models.TokenizationParams
import org.json.JSONObject

internal abstract class TokenizationRequest : JSONObjectSerializable {

    abstract val paymentInstrument: JSONObject

    companion object {
        @JvmField
        val serializer = object : JSONObjectSerializer<TokenizationRequest> {
            override fun serialize(t: TokenizationRequest): JSONObject {
                return when (t) {
                    is TokenizationCheckoutRequest ->
                        TokenizationCheckoutRequest.serializer.serialize(t)
                    is TokenizationVaultRequest -> TokenizationVaultRequest.serializer.serialize(t)
                    else -> error("Unsupported instance of $t")
                }
            }
        }
    }
}

internal fun TokenizationParams.toTokenizationRequest(): TokenizationRequest {
    return when (paymentMethodIntent) {
        PrimerSessionIntent.CHECKOUT -> TokenizationCheckoutRequest(
            paymentMethodDescriptor.toPaymentInstrument()
        )
        PrimerSessionIntent.VAULT -> TokenizationVaultRequest(
            paymentMethodDescriptor.toPaymentInstrument(),
            TokenType.MULTI_USE.name,
            paymentMethodIntent.toString()
        )
    }
}

internal data class TokenizationVaultRequest(
    override val paymentInstrument: JSONObject,
    private val tokenType: String,
    private val paymentFlow: String
) : TokenizationRequest() {

    companion object {
        private const val PAYMENT_INSTRUMENT_FIELD = "paymentInstrument"
        private const val TOKEN_TYPE_FIELD = "tokenType"
        private const val PAYMENT_FLOW_FIELD = "paymentFlow"

        @JvmField
        val serializer = object : JSONObjectSerializer<TokenizationVaultRequest> {
            override fun serialize(t: TokenizationVaultRequest): JSONObject {
                return JSONObject().apply {
                    put(PAYMENT_INSTRUMENT_FIELD, t.paymentInstrument)
                    put(TOKEN_TYPE_FIELD, t.tokenType)
                    put(PAYMENT_FLOW_FIELD, t.paymentFlow)
                }
            }
        }
    }
}

internal data class TokenizationCheckoutRequest(
    override val paymentInstrument: JSONObject
) : TokenizationRequest() {
    companion object {
        private const val PAYMENT_INSTRUMENT_FIELD = "paymentInstrument"

        @JvmField
        val serializer = object : JSONObjectSerializer<TokenizationCheckoutRequest> {
            override fun serialize(t: TokenizationCheckoutRequest): JSONObject {
                return JSONObject().apply {
                    put(PAYMENT_INSTRUMENT_FIELD, t.paymentInstrument)
                }
            }
        }
    }
}

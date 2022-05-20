package io.primer.android.data.tokenization.models

import io.primer.android.PrimerPaymentMethodIntent
import io.primer.android.domain.tokenization.models.TokenizationParams
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.json.JSONObject

@Serializable
internal abstract class TokenizationRequest {

    abstract val paymentInstrument: JsonElement
}

internal fun TokenizationParams.toTokenizationRequest(): TokenizationRequest {
    return when (paymentMethodIntent) {
        PrimerPaymentMethodIntent.CHECKOUT -> TokenizationCheckoutRequest(
            paymentMethodDescriptor.toPaymentInstrument().toJson()
        )
        PrimerPaymentMethodIntent.VAULT -> TokenizationVaultRequest(
            paymentMethodDescriptor.toPaymentInstrument()
                .toJson(),
            TokenType.MULTI_USE.name,
            paymentMethodIntent.toString()
        )
    }
}

internal fun JSONObject.toJson(): JsonElement {
    return Json.parseToJsonElement(this.toString())
}

@Serializable
@Suppress("UnusedPrivateMember")
internal data class TokenizationVaultRequest(
    override val paymentInstrument: JsonElement,
    private val tokenType: String,
    private val paymentFlow: String,
) : TokenizationRequest()

@Serializable
internal data class TokenizationCheckoutRequest(
    override val paymentInstrument: JsonElement,
) : TokenizationRequest()

internal val tokenizationSerializationModule = SerializersModule {
    polymorphic(TokenizationRequest::class) {
        subclass(
            TokenizationVaultRequest::class,
            TokenizationVaultRequest.serializer()
        )
        subclass(
            TokenizationCheckoutRequest::class,
            TokenizationCheckoutRequest.serializer()
        )
    }
}

package io.primer.android.data.tokenization.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@Serializable
internal abstract class TokenizationRequest {

    abstract val paymentInstrument: JsonElement
}

@Serializable
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

package io.primer.android.domain.tokenization.models

import io.primer.android.UXMode
import io.primer.android.data.tokenization.models.TokenizationCheckoutRequest
import io.primer.android.data.tokenization.models.TokenizationRequest
import io.primer.android.data.tokenization.models.TokenizationVaultRequest
import io.primer.android.model.dto.TokenType
import io.primer.android.payment.PaymentMethodDescriptor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.json.JSONObject

internal data class TokenizationParams(
    val paymentMethodDescriptor: PaymentMethodDescriptor,
    val uxMode: UXMode,
    val is3DSAtTokenizationEnabled: Boolean,
)

internal fun TokenizationParams.toTokenizationRequest(): TokenizationRequest {
    return when (uxMode) {
        UXMode.CHECKOUT -> TokenizationCheckoutRequest(
            paymentMethodDescriptor.toPaymentInstrument().toJson()
        )
        UXMode.VAULT -> TokenizationVaultRequest(
            paymentMethodDescriptor.toPaymentInstrument()
                .toJson(),
            TokenType.MULTI_USE.name,
            uxMode.toString()
        )
    }
}

internal fun JSONObject.toJson(): JsonElement {
    return Json.parseToJsonElement(this.toString())
}

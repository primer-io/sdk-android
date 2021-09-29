package io.primer.android.domain.tokenization.models

import io.primer.android.PaymentMethodIntent
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
    val paymentMethodIntent: PaymentMethodIntent,
    val is3DSOnVaultingEnabled: Boolean,
)

internal fun TokenizationParams.toTokenizationRequest(): TokenizationRequest {
    return when (paymentMethodIntent) {
        PaymentMethodIntent.CHECKOUT -> TokenizationCheckoutRequest(
            paymentMethodDescriptor.toPaymentInstrument().toJson()
        )
        PaymentMethodIntent.VAULT -> TokenizationVaultRequest(
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

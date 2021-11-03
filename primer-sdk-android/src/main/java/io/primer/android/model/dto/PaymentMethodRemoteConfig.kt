package io.primer.android.model.dto

import androidx.annotation.Keep
import io.primer.android.data.token.model.ClientTokenIntent
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class PaymentMethodRemoteConfig(
    val id: String? = null, // payment card has null only
    val type: PaymentMethodType = PaymentMethodType.UNKNOWN,
    val options: PaymentMethodRemoteConfigOptions? = null,
)

@Keep
@Serializable
enum class PaymentMethodType(internal val intent: ClientTokenIntent? = null) {
    PAYMENT_CARD(ClientTokenIntent.`3DS_AUTHENTICATION`),
    KLARNA,
    GOOGLE_PAY,
    PAYPAL,
    GOCARDLESS,
    APAYA,
    PAY_NL_IDEAL(ClientTokenIntent.PAY_NL_IDEAL_REDIRECTION),
    HOOLAH(ClientTokenIntent.HOOLAH_REDIRECTION),
    ADYEN_GIROPAY(ClientTokenIntent.ADYEN_GIROPAY_REDIRECTION),
    ADYEN_TWINT(ClientTokenIntent.ADYEN_TWINT_REDIRECTION),
    ADYEN_SOFORT(ClientTokenIntent.ADYEN_SOFORT_REDIRECTION),
    ADYEN_TRUSTLY(ClientTokenIntent.ADYEN_TRUSTLY_REDIRECTION),
    ADYEN_ALIPAY(ClientTokenIntent.ADYEN_ALIPAY_REDIRECTION),
    UNKNOWN;

    companion object {
        fun safeValueOf(type: String?) = values().find { type == it.name }
            ?: UNKNOWN
    }
}

@Keep
@Serializable
data class PaymentMethodRemoteConfigOptions(
    val merchantId: String? = null,
    val merchantAccountId: String? = null,
    val threeDSecureEnabled: Boolean? = null,
)

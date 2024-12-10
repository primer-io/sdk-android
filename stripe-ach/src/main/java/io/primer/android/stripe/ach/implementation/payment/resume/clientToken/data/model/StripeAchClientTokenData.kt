package io.primer.android.stripe.ach.implementation.payment.resume.clientToken.data.model

import io.primer.android.clientToken.core.errors.data.exception.ExpiredClientTokenException
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import io.primer.android.clientToken.core.validation.data.utils.ClientTokenDecoder
import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import org.json.JSONObject

internal data class StripeAchClientTokenData(
    val intent: String,
    val sdkCompleteUrl: String?,
    val stripePaymentIntentId: String?,
    val stripeClientSecret: String?
) : JSONDeserializable {

    companion object {

        @Throws(InvalidClientTokenException::class, ExpiredClientTokenException::class)
        fun fromString(encoded: String): StripeAchClientTokenData {
            ClientTokenDecoder.decode(encoded).let { decoded ->
                return JSONSerializationUtils
                    .getJsonObjectDeserializer<StripeAchClientTokenData>()
                    .deserialize(JSONObject(decoded))
            }
        }

        private const val INTENT_FIELD = "intent"
        private const val SDK_COMPLETE_URL_FIELD = "sdkCompleteUrl"
        private const val STRIPE_PAYMENT_INTENT_ID_FIELD = "stripePaymentIntentId"
        private const val STRIPE_CLIENT_SECRET_FIELD = "stripeClientSecret"

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            StripeAchClientTokenData(
                intent = t.getString(INTENT_FIELD),
                sdkCompleteUrl = t.optNullableString(SDK_COMPLETE_URL_FIELD),
                stripePaymentIntentId = t.optNullableString(STRIPE_PAYMENT_INTENT_ID_FIELD),
                stripeClientSecret = t.optNullableString(STRIPE_CLIENT_SECRET_FIELD)
            )
        }
    }
}

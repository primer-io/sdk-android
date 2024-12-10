package io.primer.android.ipay88.implementation.payment.resume.clientToken.data.model

import com.ipay.IPayIH
import io.primer.android.clientToken.core.errors.data.exception.ExpiredClientTokenException
import io.primer.android.clientToken.core.errors.data.exception.InvalidClientTokenException
import io.primer.android.clientToken.core.validation.data.utils.ClientTokenDecoder
import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import org.json.JSONObject
import java.net.URLEncoder

internal data class IPay88ClientTokenData(
    val intent: String,
    val statusUrl: String,
    val paymentId: String,
    val paymentMethod: Int,
    val actionType: String,
    val referenceNumber: String,
    val currencyCode: String,
    val countryCode: String?,
    val backendCallbackUrl: String
) : JSONDeserializable {

    companion object {

        @Throws(InvalidClientTokenException::class, ExpiredClientTokenException::class)
        fun fromString(encoded: String): IPay88ClientTokenData {
            ClientTokenDecoder.decode(encoded).let { decoded ->
                return JSONSerializationUtils
                    .getJsonObjectDeserializer<IPay88ClientTokenData>()
                    .deserialize(JSONObject(decoded))
            }
        }

        private const val INTENT_FIELD = "intent"
        private const val STATUS_URL_FIELD = "statusUrl"
        private const val BACKEND_CALLBACK_URL_FIELD = "backendCallbackUrl"
        private const val PRIMER_TRANSACTION_ID_FIELD = "primerTransactionId"
        private const val IPAY88_PAYMENT_METHOD_ID_FIELD = "iPay88PaymentMethodId"
        private const val IPAY88_ACTION_TYPE_FIELD = "iPay88ActionType"
        private const val SUPPORTED_CURRENCY_CODE_FIELD = "supportedCurrencyCode"
        private const val SUPPORTED_COUNTRY_FIELD = "supportedCountry"

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            IPay88ClientTokenData(
                intent = t.getString(INTENT_FIELD),
                statusUrl = t.getString(STATUS_URL_FIELD),
                paymentId = t.getString(IPAY88_PAYMENT_METHOD_ID_FIELD),
                paymentMethod = IPayIH.PAY_METHOD_CREDIT_CARD,
                actionType = t.getString(IPAY88_ACTION_TYPE_FIELD),
                referenceNumber = t.getString(PRIMER_TRANSACTION_ID_FIELD),
                countryCode = t.getString(SUPPORTED_COUNTRY_FIELD),
                currencyCode = t.getString(SUPPORTED_CURRENCY_CODE_FIELD),
                backendCallbackUrl = requireNotNull(
                    URLEncoder.encode(
                        t.getString(BACKEND_CALLBACK_URL_FIELD),
                        Charsets.UTF_8.name()
                    )
                )
            )
        }
    }
}

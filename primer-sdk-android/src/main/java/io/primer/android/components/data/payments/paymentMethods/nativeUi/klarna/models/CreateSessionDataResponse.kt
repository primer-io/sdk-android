package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models

import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaPaymentCategory
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.klarna.models.KlarnaSession
import io.primer.android.core.logging.WhitelistedHttpBodyKeysProvider
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.internal.dsl.whitelistedKeys
import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.sequence
import org.json.JSONObject

internal data class CreateSessionDataResponse(
    val clientToken: String,
    val sessionId: String,
    val categories: List<PaymentCategory>
) : JSONDeserializable {

    companion object {
        private const val CLIENT_TOKEN_FIELD = "clientToken"
        private const val SESSION_ID_FIELD = "sessionId"
        private const val CATEGORIES_FIELD = "categories"

        val provider = object : WhitelistedHttpBodyKeysProvider {
            override val values: List<WhitelistedKey> = whitelistedKeys {
                primitiveKey(SESSION_ID_FIELD)
                nonPrimitiveKey(CATEGORIES_FIELD) {
                    primitiveKey(PaymentCategory.IDENTIFIER_FIELD)
                    primitiveKey(PaymentCategory.NAME_FIELD)
                    primitiveKey(PaymentCategory.DESCRIPTIVE_ASSET_FIELD)
                    primitiveKey(PaymentCategory.STANDARD_ASSET_FIELD)
                }
            }
        }

        @JvmField
        val deserializer = object : JSONDeserializer<CreateSessionDataResponse> {
            override fun deserialize(t: JSONObject): CreateSessionDataResponse {
                return CreateSessionDataResponse(
                    t.getString(CLIENT_TOKEN_FIELD),
                    t.getString(SESSION_ID_FIELD),
                    t.optJSONArray(CATEGORIES_FIELD)?.sequence<JSONObject>()?.map {
                        JSONSerializationUtils
                            .getDeserializer<PaymentCategory>()
                            .deserialize(it)
                    }?.toList().orEmpty()
                )
            }
        }
    }
}

internal fun CreateSessionDataResponse.toKlarnaSession(webViewTitle: String?) = KlarnaSession(
    webViewTitle.orEmpty(),
    sessionId,
    clientToken,
    categories.map {
        KlarnaPaymentCategory(
            identifier = it.identifier,
            name = it.name,
            descriptiveAssetUrl = it.descriptiveAssetUrl,
            standardAssetUrl = it.standardAssetUrl
        )
    }
)

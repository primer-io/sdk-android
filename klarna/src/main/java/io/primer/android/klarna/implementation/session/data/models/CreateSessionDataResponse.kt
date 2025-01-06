package io.primer.android.klarna.implementation.session.data.models

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.sequence
import io.primer.android.core.logging.WhitelistedHttpBodyKeysProvider
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.internal.dsl.whitelistedKeys
import io.primer.android.klarna.implementation.session.domain.models.KlarnaPaymentCategory
import io.primer.android.klarna.implementation.session.domain.models.KlarnaSession
import org.json.JSONObject

internal data class CreateSessionDataResponse(
    val clientToken: String,
    val sessionId: String,
    val categories: List<PaymentCategory>,
) : JSONDeserializable {
    companion object {
        private const val CLIENT_TOKEN_FIELD = "clientToken"
        private const val SESSION_ID_FIELD = "sessionId"
        private const val CATEGORIES_FIELD = "categories"

        val provider =
            object : WhitelistedHttpBodyKeysProvider {
                override val values: List<WhitelistedKey> =
                    whitelistedKeys {
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
        val deserializer =
            JSONObjectDeserializer<CreateSessionDataResponse> { t ->
                CreateSessionDataResponse(
                    t.getString(CLIENT_TOKEN_FIELD),
                    t.getString(SESSION_ID_FIELD),
                    t.optJSONArray(CATEGORIES_FIELD)?.sequence<JSONObject>()?.map {
                        JSONSerializationUtils
                            .getJsonObjectDeserializer<PaymentCategory>()
                            .deserialize(it)
                    }?.toList().orEmpty(),
                )
            }
    }
}

internal fun CreateSessionDataResponse.toKlarnaSession() =
    KlarnaSession(
        sessionId,
        clientToken,
        categories.map {
            KlarnaPaymentCategory(
                identifier = it.identifier,
                name = it.name,
                descriptiveAssetUrl = it.descriptiveAssetUrl,
                standardAssetUrl = it.standardAssetUrl,
            )
        },
    )

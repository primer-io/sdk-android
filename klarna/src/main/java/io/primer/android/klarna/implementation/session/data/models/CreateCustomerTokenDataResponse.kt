package io.primer.android.klarna.implementation.session.data.models

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.JSONSerializationUtils
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import io.primer.android.core.logging.WhitelistedHttpBodyKeysProvider
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.internal.dsl.whitelistedKeys
import io.primer.android.klarna.implementation.session.data.models.KlarnaSessionData.Companion.ORDER_LINES_FIELD
import io.primer.android.klarna.implementation.session.data.models.SessionOrderLines.Companion.QUANTITY_FIELD
import io.primer.android.klarna.implementation.session.data.models.SessionOrderLines.Companion.TOTAL_AMOUNT_FIELD
import io.primer.android.klarna.implementation.session.data.models.SessionOrderLines.Companion.TOTAL_DISCOUNT_AMOUNT_FIELD
import io.primer.android.klarna.implementation.session.data.models.SessionOrderLines.Companion.TYPE_FIELD
import io.primer.android.klarna.implementation.session.data.models.SessionOrderLines.Companion.UNIT_PRICE_FIELD

internal data class CreateCustomerTokenDataResponse(
    val customerTokenId: String?,
    val sessionData: KlarnaSessionData,
) : JSONDeserializable {
    companion object {
        private const val CUSTOMER_TOKEN_ID_FIELD = "customerTokenId"
        private const val SESSION_DATA_FIELD = "sessionData"

        val provider =
            object : WhitelistedHttpBodyKeysProvider {
                override val values: List<WhitelistedKey> =
                    whitelistedKeys {
                        nonPrimitiveKey(SESSION_DATA_FIELD) {
                            nonPrimitiveKey(ORDER_LINES_FIELD) {
                                primitiveKey(TYPE_FIELD)
                                primitiveKey(QUANTITY_FIELD)
                                primitiveKey(UNIT_PRICE_FIELD)
                                primitiveKey(TOTAL_AMOUNT_FIELD)
                                primitiveKey(TOTAL_DISCOUNT_AMOUNT_FIELD)
                            }
                        }
                    }
            }

        @JvmField
        val deserializer =
            JSONObjectDeserializer<CreateCustomerTokenDataResponse> { t ->
                CreateCustomerTokenDataResponse(
                    t.optNullableString(CUSTOMER_TOKEN_ID_FIELD),
                    JSONSerializationUtils
                        .getJsonObjectDeserializer<KlarnaSessionData>().deserialize(
                            t.getJSONObject(SESSION_DATA_FIELD),
                        ),
                )
            }
    }
}

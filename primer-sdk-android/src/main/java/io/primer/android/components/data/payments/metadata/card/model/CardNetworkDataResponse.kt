package io.primer.android.components.data.payments.metadata.card.model

import io.primer.android.core.logging.WhitelistedHttpBodyKeysProvider
import io.primer.android.core.logging.internal.WhitelistedKey
import io.primer.android.core.logging.internal.dsl.whitelistedKeys
import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONObjectDeserializer
import io.primer.android.core.serialization.json.JSONSerializationUtils
import io.primer.android.core.serialization.json.extensions.sequence
import io.primer.android.ui.CardNetwork
import org.json.JSONObject

internal data class CardNetworkDataResponse(
    val displayName: String,
    val value: CardNetwork.Type?
) : JSONDeserializable {

    companion object {

        const val DISPLAY_NAME_FIELD = "displayName"
        const val VALUE_FIELD = "value"

        @JvmField
        val deserializer = JSONObjectDeserializer {
            CardNetworkDataResponse(
                it.getString(DISPLAY_NAME_FIELD),
                CardNetwork.Type.valueOrNull(it.getString(VALUE_FIELD))
            )
        }
    }
}

internal data class CardBinMetadataDataNetworksResponse(
    val networks: List<CardNetworkDataResponse>
) : JSONDeserializable {

    companion object {

        private const val NETWORKS_FIELD = "networks"

        @JvmField
        val deserializer = JSONObjectDeserializer {
            CardBinMetadataDataNetworksResponse(
                it.getJSONArray(NETWORKS_FIELD).sequence<JSONObject>()
                    .map { networkResponse ->
                        JSONSerializationUtils
                            .getJsonObjectDeserializer<CardNetworkDataResponse>()
                            .deserialize(networkResponse)
                    }.toList()
            )
        }

        val provider = object : WhitelistedHttpBodyKeysProvider {
            override val values: List<WhitelistedKey> = whitelistedKeys {
                nonPrimitiveKey(NETWORKS_FIELD) {
                    primitiveKey(CardNetworkDataResponse.DISPLAY_NAME_FIELD)
                    primitiveKey(CardNetworkDataResponse.VALUE_FIELD)
                }
            }
        }
    }
}

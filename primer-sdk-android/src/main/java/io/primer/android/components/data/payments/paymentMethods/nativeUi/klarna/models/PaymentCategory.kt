package io.primer.android.components.data.payments.paymentMethods.nativeUi.klarna.models

import io.primer.android.core.serialization.json.JSONDeserializable

import io.primer.android.core.serialization.json.JSONObjectDeserializer

internal data class PaymentCategory(
    val identifier: String,
    val name: String,
    val descriptiveAssetUrl: String,
    val standardAssetUrl: String
) : JSONDeserializable {

    companion object {
        const val IDENTIFIER_FIELD = "identifier"
        const val NAME_FIELD = "name"
        const val DESCRIPTIVE_ASSET_FIELD = "descriptiveAssetUrl"
        const val STANDARD_ASSET_FIELD = "standardAssetUrl"

        @JvmField
        val deserializer = JSONObjectDeserializer { t ->
            PaymentCategory(
                t.getString(IDENTIFIER_FIELD),
                t.getString(NAME_FIELD),
                t.getString(DESCRIPTIVE_ASSET_FIELD),
                t.getString(STANDARD_ASSET_FIELD)
            )
        }
    }
}

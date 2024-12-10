package io.primer.android.vouchers.retailOutlets.implementation.tokenization.data.model

import io.primer.android.core.data.serialization.json.JSONObjectSerializable
import io.primer.android.core.data.serialization.json.JSONObjectSerializer
import org.json.JSONObject

internal data class RetailOutletsSessionInfoDataRequest(
    val retailerOutlet: String,
    val locale: String,
    val platform: String = "ANDROID"
) : JSONObjectSerializable {

    companion object {

        private const val PLATFORM_FIELD = "platform"
        private const val LOCALE_FIELD = "locale"
        private const val RETAIL_OUTLET_FIELD = "retailOutlet"

        @JvmField
        val serializer = JSONObjectSerializer<RetailOutletsSessionInfoDataRequest> { t ->
            JSONObject().apply {
                put(PLATFORM_FIELD, t.platform)
                put(LOCALE_FIELD, t.locale)
                put(RETAIL_OUTLET_FIELD, t.retailerOutlet)
            }
        }
    }
}

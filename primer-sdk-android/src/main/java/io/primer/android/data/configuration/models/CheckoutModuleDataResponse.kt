package io.primer.android.data.configuration.models

import io.primer.android.core.serialization.json.JSONDeserializable
import io.primer.android.core.serialization.json.JSONDeserializer
import io.primer.android.core.serialization.json.extensions.optNullableString
import io.primer.android.core.serialization.json.extensions.toBooleanMap
import io.primer.android.domain.session.models.CheckoutModule
import org.json.JSONObject

internal data class CheckoutModuleDataResponse(
    val type: CheckoutModuleType,
    val requestUrl: String?,
    val options: Map<String, Boolean>?
) : JSONDeserializable {

    fun toCheckoutModule() = CheckoutModule(type, options)

    companion object {
        const val TYPE_FIELD = "type"
        const val REQUEST_URL_FIELD = "requestUrl"
        const val OPTIONS_FIELD = "options"

        @JvmField
        val deserializer = object : JSONDeserializer<CheckoutModuleDataResponse> {

            override fun deserialize(t: JSONObject): CheckoutModuleDataResponse {
                return CheckoutModuleDataResponse(
                    CheckoutModuleType.safeValueOf(t.optNullableString(TYPE_FIELD)),
                    t.optNullableString(REQUEST_URL_FIELD),
                    t.optJSONObject(OPTIONS_FIELD)?.toBooleanMap()
                )
            }
        }
    }
}

internal enum class CheckoutModuleType {

    BILLING_ADDRESS,
    CARD_INFORMATION,
    UNKNOWN;

    companion object {
        fun safeValueOf(value: String?) = values().find { it.name == value } ?: UNKNOWN
    }
}

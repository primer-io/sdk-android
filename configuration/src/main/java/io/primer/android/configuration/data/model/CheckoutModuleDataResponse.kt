package io.primer.android.configuration.data.model

import io.primer.android.core.data.serialization.json.JSONDeserializable
import io.primer.android.core.data.serialization.json.JSONObjectDeserializer
import io.primer.android.core.data.serialization.json.extensions.optNullableString
import io.primer.android.core.data.serialization.json.extensions.toBooleanMap

data class CheckoutModuleDataResponse(
    val type: CheckoutModuleType,
    val requestUrl: String?,
    val options: Map<String, Boolean>?,
    val shippingOptions: ShippingOptions?,
) : JSONDeserializable {
    companion object {
        const val TYPE_FIELD = "type"
        const val REQUEST_URL_FIELD = "requestUrl"
        const val OPTIONS_FIELD = "options"
        private const val SHIPPING_METHODS_FIELD = "shippingMethods"
        private const val SELECTED_SHIPPING_METHOD_FIELD = "selectedShippingMethod"
        private const val NAME_FIELD = "name"
        private const val DESCRIPTION_FIELD = "description"
        private const val AMOUNT_FIELD = "amount"
        private const val ID_FIELD = "id"

        @JvmField
        val deserializer =
            JSONObjectDeserializer { jsonObject ->
                val type = CheckoutModuleType.safeValueOf(jsonObject.optNullableString(TYPE_FIELD))
                val options =
                    if (type != CheckoutModuleType.SHIPPING) {
                        jsonObject.optJSONObject(OPTIONS_FIELD)?.toBooleanMap()
                    } else {
                        null
                    }

                val shippingOptions =
                    if (type == CheckoutModuleType.SHIPPING) {
                        val optionsObject = jsonObject.optJSONObject(OPTIONS_FIELD)
                        val shippingMethodsArray = optionsObject?.optJSONArray(SHIPPING_METHODS_FIELD)
                        val shippingMethods = mutableListOf<ShippingMethod>()

                        for (i in 0 until (shippingMethodsArray?.length() ?: 0)) {
                            val methodJson = shippingMethodsArray?.optJSONObject(i)
                            methodJson?.run {
                                shippingMethods.add(
                                    ShippingMethod(
                                        optString(NAME_FIELD),
                                        optString(DESCRIPTION_FIELD),
                                        optInt(AMOUNT_FIELD),
                                        optString(ID_FIELD),
                                    ),
                                )
                            }
                        }
                        if (shippingMethods.isNotEmpty()) {
                            ShippingOptions(
                                shippingMethods,
                                optionsObject?.optString(SELECTED_SHIPPING_METHOD_FIELD),
                            )
                        } else {
                            null
                        }
                    } else {
                        null
                    }

                CheckoutModuleDataResponse(
                    type,
                    jsonObject.optNullableString(REQUEST_URL_FIELD),
                    options,
                    shippingOptions,
                )
            }
    }
}

enum class CheckoutModuleType {
    BILLING_ADDRESS,
    CARD_INFORMATION,
    SHIPPING,
    UNKNOWN,
    ;

    companion object {
        fun safeValueOf(value: String?) = entries.find { it.name == value } ?: UNKNOWN
    }
}

data class ShippingOptions(
    val shippingMethods: List<ShippingMethod>,
    val selectedShippingMethod: String?,
)

data class ShippingMethod(
    val name: String,
    val description: String,
    val amount: Int,
    val id: String,
)

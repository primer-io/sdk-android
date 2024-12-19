package io.primer.android.googlepay.implementation.utils

import org.json.JSONArray
import org.json.JSONObject

internal object GooglePayPayloadUtils {

    fun baseCardPaymentMethod(
        allowedCardNetworks: List<String>,
        allowedCardAuthMethods: List<String>,
        billingAddressRequired: Boolean
    ): JSONObject {
        return JSONObject().apply {
            val parameters = JSONObject().apply {
                put("allowedAuthMethods", JSONArray(allowedCardAuthMethods))
                put("allowedCardNetworks", JSONArray(allowedCardNetworks))
                put("billingAddressRequired", billingAddressRequired)
                if (billingAddressRequired) {
                    put(
                        "billingAddressParameters",
                        JSONObject().apply {
                            put("format", "FULL")
                        }
                    )
                }
            }

            put("type", "CARD")
            put("parameters", parameters)
        }
    }
}

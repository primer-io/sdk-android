package io.primer.android.components.domain.inputs.models

import io.primer.android.utils.sanitized
import org.json.JSONObject

enum class PrimerInputElementType(val field: String) {
    ALL("all"),
    CARD_NUMBER("number"),
    CVV("cvv"),
    EXPIRY_DATE("expiryDate"),
    EXPIRY_MONTH("expirationMonth"),
    EXPIRY_YEAR("expirationYear"),
    CARDHOLDER_NAME("cardholderName"),
    POSTAL_CODE("postalCode"),
    COUNTRY_CODE("countryCode"),
    CITY("city"),
    STATE("state"),
    ADDRESS_LINE_1("addressLine1"),
    ADDRESS_LINE_2("addressLine2"),
    PHONE_NUMBER("phoneNumber"),
    FIRST_NAME("firstName"),
    LAST_NAME("lastName");

    companion object {
        fun fieldOf(key: String): PrimerInputElementType? {
            return values().firstOrNull { it.field == key }
        }
    }
}

internal fun Map<String, Boolean>?.via(type: PrimerInputElementType): Boolean? {
    return this?.get(type.field)
}

internal fun <T : Any> JSONObject.putFor(type: PrimerInputElementType, value: T) {
    this.put(type.field, value)
}

internal fun JSONObject.valueBy(key: String): String = this.optString(key)

internal fun JSONObject.valueBy(type: PrimerInputElementType) = this.valueBy(type.field).sanitized()

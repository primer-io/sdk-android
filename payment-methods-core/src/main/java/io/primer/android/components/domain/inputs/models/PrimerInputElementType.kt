package io.primer.android.components.domain.inputs.models

enum class PrimerInputElementType(val field: String) {
    ALL("all"),
    CARD_NUMBER("number"),
    CVV("cvv"),
    EXPIRY_DATE("expiryDate"),
    CARDHOLDER_NAME("cardholderName"),
    POSTAL_CODE("postalCode"),
    COUNTRY_CODE("countryCode"),
    CITY("city"),
    STATE("state"),
    ADDRESS_LINE_1("addressLine1"),
    ADDRESS_LINE_2("addressLine2"),
    PHONE_NUMBER("phoneNumber"),
    FIRST_NAME("firstName"),
    LAST_NAME("lastName"),
    RETAIL_OUTLET("retailOutlet"),
    OTP_CODE("otpCode");

    companion object {
        fun fieldOf(key: String): PrimerInputElementType? {
            return PrimerInputElementType.entries.firstOrNull { it.field == key }
        }
    }
}

fun Map<String, Boolean>?.isEnabled(type: PrimerInputElementType): Boolean {
    return this?.get(type.field) == true
}

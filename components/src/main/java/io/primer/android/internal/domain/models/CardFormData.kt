package io.primer.android.internal.domain.models

import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.configuration.data.model.CardNetwork

internal data class CardFormData(
    val cardNumber: String = "",
    val cvv: String = "",
    val expiryDate: String = "",
    val cardholderName: String = "",
    val postalCode: String = "",
    val countryCode: String = "",
    val city: String = "",
    val state: String = "",
    val addressLine1: String = "",
    val addressLine2: String = "",
    val phoneNumber: String = "",
    val firstName: String = "",
    val lastName: String = "",
    val retailOutlet: String = "",
    val otpCode: String = "",
    val cardNetwork: CardNetwork.Type = CardNetwork.Type.OTHER
) {
    fun toMap(): Map<PrimerInputElementType, String> = buildMap {
        if (cardNumber.isNotEmpty()) put(PrimerInputElementType.CARD_NUMBER, cardNumber)
        if (cvv.isNotEmpty()) put(PrimerInputElementType.CVV, cvv)
        if (expiryDate.isNotEmpty()) put(PrimerInputElementType.EXPIRY_DATE, expiryDate)
        if (cardholderName.isNotEmpty()) put(PrimerInputElementType.CARDHOLDER_NAME, cardholderName)
        if (postalCode.isNotEmpty()) put(PrimerInputElementType.POSTAL_CODE, postalCode)
        if (countryCode.isNotEmpty()) put(PrimerInputElementType.COUNTRY_CODE, countryCode)
        if (city.isNotEmpty()) put(PrimerInputElementType.CITY, city)
        if (state.isNotEmpty()) put(PrimerInputElementType.STATE, state)
        if (addressLine1.isNotEmpty()) put(PrimerInputElementType.ADDRESS_LINE_1, addressLine1)
        if (addressLine2.isNotEmpty()) put(PrimerInputElementType.ADDRESS_LINE_2, addressLine2)
        if (phoneNumber.isNotEmpty()) put(PrimerInputElementType.PHONE_NUMBER, phoneNumber)
        if (firstName.isNotEmpty()) put(PrimerInputElementType.FIRST_NAME, firstName)
        if (lastName.isNotEmpty()) put(PrimerInputElementType.LAST_NAME, lastName)
        if (retailOutlet.isNotEmpty()) put(PrimerInputElementType.RETAIL_OUTLET, retailOutlet)
        if (otpCode.isNotEmpty()) put(PrimerInputElementType.OTP_CODE, otpCode)
    }

    fun updateField(field: PrimerInputElementType, value: String): CardFormData = when (field) {
        PrimerInputElementType.CARD_NUMBER -> copy(cardNumber = value)
        PrimerInputElementType.CVV -> copy(cvv = value)
        PrimerInputElementType.EXPIRY_DATE -> copy(expiryDate = value)
        PrimerInputElementType.CARDHOLDER_NAME -> copy(cardholderName = value)
        PrimerInputElementType.POSTAL_CODE -> copy(postalCode = value)
        PrimerInputElementType.COUNTRY_CODE -> copy(countryCode = value)
        PrimerInputElementType.CITY -> copy(city = value)
        PrimerInputElementType.STATE -> copy(state = value)
        PrimerInputElementType.ADDRESS_LINE_1 -> copy(addressLine1 = value)
        PrimerInputElementType.ADDRESS_LINE_2 -> copy(addressLine2 = value)
        PrimerInputElementType.PHONE_NUMBER -> copy(phoneNumber = value)
        PrimerInputElementType.FIRST_NAME -> copy(firstName = value)
        PrimerInputElementType.LAST_NAME -> copy(lastName = value)
        PrimerInputElementType.RETAIL_OUTLET -> copy(retailOutlet = value)
        PrimerInputElementType.OTP_CODE -> copy(otpCode = value)
        else -> this // For any unhandled types
    }
}
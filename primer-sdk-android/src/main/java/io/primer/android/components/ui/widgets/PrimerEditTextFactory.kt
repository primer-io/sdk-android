package io.primer.android.components.ui.widgets

import android.content.Context
import io.primer.android.model.dto.PrimerInputFieldType

class PrimerEditTextFactory private constructor() {

    companion object {

        fun createFromType(
            context: Context,
            inputFieldType: PrimerInputFieldType
        ): PrimerEditText {
            return when (inputFieldType) {
                PrimerInputFieldType.CARD_NUMBER -> PrimerCardNumberEditText(context)
                PrimerInputFieldType.EXPIRY_DATE -> PrimerExpiryEditText(context)
                PrimerInputFieldType.CVV -> PrimerCvvEditText(context)
                PrimerInputFieldType.CARDHOLDER_NAME -> PrimerCardholderNameEditText(context)
                PrimerInputFieldType.FIRST_NAME -> PrimerFirstNameEditText(context)
                PrimerInputFieldType.LAST_NAME -> PrimerLastNameEditText(context)
                PrimerInputFieldType.COUNTRY_CODE -> PrimerCountryCodeEditText(context)
                PrimerInputFieldType.POSTAL_CODE -> PrimerPostalCodeEditText(context)
                PrimerInputFieldType.STATE -> PrimerStateEditText(context)
                PrimerInputFieldType.CITY -> PrimerCityEditText(context)
                PrimerInputFieldType.ADDRESS_LINE_1 -> PrimerAddressLine1EditText(context)
                PrimerInputFieldType.ADDRESS_LINE_2 -> PrimerAddressLine2EditText(context)
                PrimerInputFieldType.PHONE_NUMBER -> PrimerPhoneNumberEditText(context)
                PrimerInputFieldType.ALL -> PrimerOtherEditText(context)
                PrimerInputFieldType.EXPIRY_MONTH -> PrimerOtherEditText(context)
                PrimerInputFieldType.EXPIRY_YEAR -> PrimerOtherEditText(context)
            }
        }
    }
}

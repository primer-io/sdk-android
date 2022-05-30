package io.primer.android.components.ui.widgets

import android.content.Context
import io.primer.android.components.domain.inputs.models.PrimerInputElementType

class PrimerEditTextFactory private constructor() {

    companion object {

        fun createFromType(
            context: Context,
            inputFieldType: PrimerInputElementType
        ): PrimerEditText {
            return when (inputFieldType) {
                PrimerInputElementType.CARD_NUMBER -> PrimerCardNumberEditText(context)
                PrimerInputElementType.EXPIRY_DATE -> PrimerExpiryEditText(context)
                PrimerInputElementType.CVV -> PrimerCvvEditText(context)
                PrimerInputElementType.CARDHOLDER_NAME -> PrimerCardholderNameEditText(context)
                PrimerInputElementType.FIRST_NAME -> PrimerFirstNameEditText(context)
                PrimerInputElementType.LAST_NAME -> PrimerLastNameEditText(context)
                PrimerInputElementType.COUNTRY_CODE -> PrimerCountryCodeEditText(context)
                PrimerInputElementType.POSTAL_CODE -> PrimerPostalCodeEditText(context)
                PrimerInputElementType.STATE -> PrimerStateEditText(context)
                PrimerInputElementType.CITY -> PrimerCityEditText(context)
                PrimerInputElementType.ADDRESS_LINE_1 -> PrimerAddressLine1EditText(context)
                PrimerInputElementType.ADDRESS_LINE_2 -> PrimerAddressLine2EditText(context)
                PrimerInputElementType.PHONE_NUMBER -> PrimerPhoneNumberEditText(context)
                PrimerInputElementType.ALL -> PrimerOtherEditText(context)
                PrimerInputElementType.EXPIRY_MONTH -> PrimerOtherEditText(context)
                PrimerInputElementType.EXPIRY_YEAR -> PrimerOtherEditText(context)
            }
        }
    }
}

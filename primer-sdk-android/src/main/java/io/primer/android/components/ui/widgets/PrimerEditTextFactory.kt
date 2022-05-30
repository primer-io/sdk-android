package io.primer.android.components.ui.widgets

import android.content.Context
import io.primer.android.ExperimentalPrimerApi
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.ui.widgets.elements.PrimerInputElement

class PrimerEditTextFactory private constructor() {

    companion object {

        @OptIn(ExperimentalPrimerApi::class)
        fun createFromType(
            context: Context,
            inputFieldType: PrimerInputElementType
        ): PrimerInputElement {
            return when (inputFieldType) {
                PrimerInputElementType.CARD_NUMBER -> PrimerCardNumberEditText(context)
                PrimerInputElementType.EXPIRY_DATE -> PrimerExpiryEditText(context)
                PrimerInputElementType.CVV -> PrimerCvvEditText(context)
                PrimerInputElementType.CARDHOLDER_NAME -> PrimerCardholderNameEditText(context)
                PrimerInputElementType.FIRST_NAME -> PrimerFirstNameEditText(context)
                PrimerInputElementType.LAST_NAME -> PrimerLastNameEditText(context)
                PrimerInputElementType.COUNTRY_CODE -> PrimerCountryCodeTextView(context)
                PrimerInputElementType.POSTAL_CODE -> PrimerPostalCodeEditText(context)
                PrimerInputElementType.STATE -> PrimerStateEditText(context)
                PrimerInputElementType.CITY -> PrimerCityEditText(context)
                PrimerInputElementType.ADDRESS_LINE_1 -> PrimerAddressLine1EditText(context)
                PrimerInputElementType.ADDRESS_LINE_2 -> PrimerAddressLine2EditText(context)
                PrimerInputElementType.PHONE_NUMBER -> PrimerPhoneNumberEditText(context)
                else -> throw IllegalStateException("Selected input type not support sdk view")
            }
        }
    }
}

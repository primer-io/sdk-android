package io.primer.android.components.ui.widgets

import android.content.Context
import io.primer.android.components.domain.inputs.models.PrimerInputElementType

@Deprecated(
    "Card components will no longer receive ongoing maintenance and will be removed in future."
)
class PrimerEditTextFactory private constructor() {

    companion object {

        fun createFromType(
            context: Context,
            inputElementType: PrimerInputElementType
        ): PrimerEditText {
            return when (inputElementType) {
                PrimerInputElementType.CARD_NUMBER -> PrimerCardNumberEditText(context)
                PrimerInputElementType.EXPIRY_DATE -> PrimerExpiryEditText(context)
                PrimerInputElementType.CVV -> PrimerCvvEditText(context)
                PrimerInputElementType.CARDHOLDER_NAME -> PrimerCardholderNameEditText(context)
                PrimerInputElementType.POSTAL_CODE -> PrimerPostalCodeEditText(context)
                else -> throw IllegalStateException("Selected input type is not supported.")
            }
        }
    }
}

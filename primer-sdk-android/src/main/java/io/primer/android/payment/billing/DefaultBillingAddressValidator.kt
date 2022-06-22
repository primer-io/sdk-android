package io.primer.android.payment.billing

import io.primer.android.R
import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.model.SyncValidationError
import io.primer.android.utils.sanitized

internal class DefaultBillingAddressValidator : BillingAddressValidator {

    override fun validate(
        billingAddressFields: Map<PrimerInputElementType, String?>,
        availableFields: Map<String, Boolean>
    ): List<SyncValidationError> {
        val errors = mutableListOf<SyncValidationError>()

        errors.checkPutError(
            PrimerInputElementType.POSTAL_CODE,
            availableFields,
            billingAddressFields
        )

        errors.checkPutError(
            PrimerInputElementType.COUNTRY_CODE,
            availableFields,
            billingAddressFields
        )

        errors.checkPutError(
            PrimerInputElementType.FIRST_NAME,
            availableFields,
            billingAddressFields
        )

        errors.checkPutError(
            PrimerInputElementType.LAST_NAME,
            availableFields,
            billingAddressFields
        )

        errors.checkPutError(
            PrimerInputElementType.ADDRESS_LINE_1,
            availableFields,
            billingAddressFields
        )

        errors.checkPutError(
            PrimerInputElementType.CITY,
            availableFields,
            billingAddressFields
        )

        errors.checkPutError(
            PrimerInputElementType.STATE,
            availableFields,
            billingAddressFields
        )

        return errors
    }
}

private fun MutableList<SyncValidationError>.checkPutError(
    inputType: PrimerInputElementType,
    availableFields: Map<String, Boolean>,
    billingAddressFields: Map<PrimerInputElementType, String?>
) {
    if (availableFields[inputType.field] == true) {
        val fieldValue = billingAddressFields.valueBy(inputType)

        if (fieldValue.isBlank()) {
            add(
                SyncValidationError(
                    name = inputType.field,
                    errorId = errorIdBy(inputType),
                    fieldId = fieldIdBy(inputType)
                )
            )
        }
    }
}

@Suppress("ComplexMethod")
private fun errorIdBy(inputType: PrimerInputElementType): Int = when (inputType) {
    PrimerInputElementType.POSTAL_CODE -> R.string.postalCodeErrorRequired
    PrimerInputElementType.COUNTRY_CODE -> R.string.countryCodeErrorRequired
    PrimerInputElementType.CITY -> R.string.cityErrorRequired
    PrimerInputElementType.STATE -> R.string.stateErrorRequired
    PrimerInputElementType.ADDRESS_LINE_1 -> R.string.addressLine1ErrorRequired
    PrimerInputElementType.FIRST_NAME -> R.string.firstNameErrorRequired
    PrimerInputElementType.LAST_NAME -> R.string.lastNameErrorRequired
    else -> R.string.error_default
}

@Suppress("ComplexMethod")
private fun fieldIdBy(inputType: PrimerInputElementType): Int = when (inputType) {
    PrimerInputElementType.POSTAL_CODE -> R.string.card_zip
    PrimerInputElementType.COUNTRY_CODE -> R.string.countryLabel
    PrimerInputElementType.CITY -> R.string.cityLabel
    PrimerInputElementType.STATE -> R.string.stateLabel
    PrimerInputElementType.ADDRESS_LINE_1 -> R.string.addressLine1
    PrimerInputElementType.FIRST_NAME -> R.string.firstNameLabel
    PrimerInputElementType.LAST_NAME -> R.string.lastNameLabel
    else -> R.string.error_default
}

internal fun Map<PrimerInputElementType, String?>.valueBy(type: PrimerInputElementType): String {
    return this[type]?.sanitized().orEmpty()
}

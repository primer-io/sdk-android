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
    PrimerInputElementType.POSTAL_CODE,
    PrimerInputElementType.COUNTRY_CODE,
    PrimerInputElementType.CITY,
    PrimerInputElementType.STATE,
    PrimerInputElementType.ADDRESS_LINE_1,
    PrimerInputElementType.FIRST_NAME,
    PrimerInputElementType.LAST_NAME -> R.string.form_error_required
    else -> R.string.error_default
}

@Suppress("ComplexMethod")
private fun fieldIdBy(inputType: PrimerInputElementType): Int = when (inputType) {
    PrimerInputElementType.POSTAL_CODE -> R.string.card_zip
    PrimerInputElementType.COUNTRY_CODE -> R.string.address_country_code
    PrimerInputElementType.CITY -> R.string.address_city
    PrimerInputElementType.STATE -> R.string.address_state
    PrimerInputElementType.ADDRESS_LINE_1 -> R.string.address_line_1
    PrimerInputElementType.FIRST_NAME -> R.string.first_name
    PrimerInputElementType.LAST_NAME -> R.string.last_name
    else -> R.string.error_default
}

internal fun Map<PrimerInputElementType, String?>.valueBy(type: PrimerInputElementType): String {
    return this[type]?.sanitized().orEmpty()
}

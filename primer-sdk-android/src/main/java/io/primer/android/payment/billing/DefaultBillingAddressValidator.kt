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
        if (availableFields[PrimerInputElementType.POSTAL_CODE.field] == true) {
            val fieldValue = billingAddressFields.valueBy(PrimerInputElementType.POSTAL_CODE)

            if (fieldValue.isBlank()) {
                errors.add(
                    SyncValidationError(
                        name = PrimerInputElementType.POSTAL_CODE.field,
                        errorId = R.string.form_error_required,
                        fieldId = R.string.card_zip
                    )
                )
            }
        }

        if (availableFields[PrimerInputElementType.COUNTRY_CODE.field] == true) {
            val fieldValue = billingAddressFields.valueBy(PrimerInputElementType.COUNTRY_CODE)

            if (fieldValue.isBlank()) {
                errors.add(
                    SyncValidationError(
                        name = PrimerInputElementType.COUNTRY_CODE.field,
                        errorId = R.string.form_error_required,
                        fieldId = R.string.address_country_code
                    )
                )
            }
        }

        if (availableFields[PrimerInputElementType.FIRST_NAME.field] == true) {
            val fieldValue = billingAddressFields.valueBy(PrimerInputElementType.FIRST_NAME)

            if (fieldValue.isBlank()) {
                errors.add(
                    SyncValidationError(
                        name = PrimerInputElementType.FIRST_NAME.field,
                        errorId = R.string.form_error_required,
                        fieldId = R.string.first_name
                    )
                )
            }
        }

        if (availableFields[PrimerInputElementType.LAST_NAME.field] == true) {
            val fieldValue = billingAddressFields.valueBy(PrimerInputElementType.LAST_NAME)

            if (fieldValue.isBlank()) {
                errors.add(
                    SyncValidationError(
                        name = PrimerInputElementType.LAST_NAME.field,
                        errorId = R.string.form_error_required,
                        fieldId = R.string.last_name
                    )
                )
            }
        }

        if (availableFields[PrimerInputElementType.ADDRESS_LINE_1.field] == true) {
            val fieldValue = billingAddressFields.valueBy(PrimerInputElementType.ADDRESS_LINE_1)

            if (fieldValue.isBlank()) {
                errors.add(
                    SyncValidationError(
                        name = PrimerInputElementType.ADDRESS_LINE_1.field,
                        errorId = R.string.form_error_required,
                        fieldId = R.string.address_line_1
                    )
                )
            }
        }

        if (availableFields[PrimerInputElementType.CITY.field] == true) {
            val fieldValue = billingAddressFields.valueBy(PrimerInputElementType.CITY)

            if (fieldValue.isBlank()) {
                errors.add(
                    SyncValidationError(
                        name = PrimerInputElementType.CITY.field,
                        errorId = R.string.form_error_required,
                        fieldId = R.string.address_city
                    )
                )
            }
        }

        if (availableFields[PrimerInputElementType.STATE.field] == true) {
            val fieldValue = billingAddressFields.valueBy(PrimerInputElementType.STATE)

            if (fieldValue.isBlank()) {
                errors.add(
                    SyncValidationError(
                        name = PrimerInputElementType.STATE.field,
                        errorId = R.string.form_error_required,
                        fieldId = R.string.address_state
                    )
                )
            }
        }
        return errors
    }
}

internal fun Map<PrimerInputElementType, String?>.valueBy(type: PrimerInputElementType): String {
    return this[type]?.sanitized().orEmpty()
}

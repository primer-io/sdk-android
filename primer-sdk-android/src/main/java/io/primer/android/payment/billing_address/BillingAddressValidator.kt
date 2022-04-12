package io.primer.android.payment.billing_address

import io.primer.android.R
import io.primer.android.model.dto.PrimerInputFieldType
import io.primer.android.model.dto.SyncValidationError
import io.primer.android.utils.sanitized

internal class BillingAddressValidator : IBillingAddressValidator {

    override fun validate(
        billingAddressFields: Map<PrimerInputFieldType, String?>,
        availableFields: Map<String, Boolean>
    ): List<SyncValidationError> {
        val errors = mutableListOf<SyncValidationError>()
        if (availableFields[PrimerInputFieldType.POSTAL_CODE.field] == true) {
            val fieldValue = billingAddressFields.valueBy(PrimerInputFieldType.POSTAL_CODE)

            if (fieldValue.isBlank()) {
                errors.add(
                    SyncValidationError(
                        name = PrimerInputFieldType.POSTAL_CODE.field,
                        errorId = R.string.form_error_required,
                        fieldId = R.string.card_zip
                    )
                )
            }
        }

        if (availableFields[PrimerInputFieldType.COUNTRY_CODE.field] == true) {
            val fieldValue = billingAddressFields.valueBy(PrimerInputFieldType.COUNTRY_CODE)

            if (fieldValue.isBlank()) {
                errors.add(
                    SyncValidationError(
                        name = PrimerInputFieldType.COUNTRY_CODE.field,
                        errorId = R.string.form_error_required,
                        fieldId = R.string.address_country_code
                    )
                )
            }
        }

        if (availableFields[PrimerInputFieldType.FIRST_NAME.field] == true) {
            val fieldValue = billingAddressFields.valueBy(PrimerInputFieldType.FIRST_NAME)

            if (fieldValue.isBlank()) {
                errors.add(
                    SyncValidationError(
                        name = PrimerInputFieldType.FIRST_NAME.field,
                        errorId = R.string.form_error_required,
                        fieldId = R.string.first_name
                    )
                )
            }
        }

        if (availableFields[PrimerInputFieldType.LAST_NAME.field] == true) {
            val fieldValue = billingAddressFields.valueBy(PrimerInputFieldType.LAST_NAME)

            if (fieldValue.isBlank()) {
                errors.add(
                    SyncValidationError(
                        name = PrimerInputFieldType.LAST_NAME.field,
                        errorId = R.string.form_error_required,
                        fieldId = R.string.last_name
                    )
                )
            }
        }

        if (availableFields[PrimerInputFieldType.ADDRESS_LINE_1.field] == true) {
            val fieldValue = billingAddressFields.valueBy(PrimerInputFieldType.ADDRESS_LINE_1)

            if (fieldValue.isBlank()) {
                errors.add(
                    SyncValidationError(
                        name = PrimerInputFieldType.ADDRESS_LINE_1.field,
                        errorId = R.string.form_error_required,
                        fieldId = R.string.address_line_1
                    )
                )
            }
        }

        if (availableFields[PrimerInputFieldType.ADDRESS_LINE_2.field] == true) {
            val fieldValue = billingAddressFields.valueBy(PrimerInputFieldType.ADDRESS_LINE_2)

            if (fieldValue.isBlank()) {
                errors.add(
                    SyncValidationError(
                        name = PrimerInputFieldType.ADDRESS_LINE_2.field,
                        errorId = R.string.form_error_required,
                        fieldId = R.string.address_line_2
                    )
                )
            }
        }

        if (availableFields[PrimerInputFieldType.CITY.field] == true) {
            val fieldValue = billingAddressFields.valueBy(PrimerInputFieldType.CITY)

            if (fieldValue.isBlank()) {
                errors.add(
                    SyncValidationError(
                        name = PrimerInputFieldType.CITY.field,
                        errorId = R.string.form_error_required,
                        fieldId = R.string.address_city
                    )
                )
            }
        }

        if (availableFields[PrimerInputFieldType.STATE.field] == true) {
            val fieldValue = billingAddressFields.valueBy(PrimerInputFieldType.STATE)

            if (fieldValue.isBlank()) {
                errors.add(
                    SyncValidationError(
                        name = PrimerInputFieldType.STATE.field,
                        errorId = R.string.form_error_required,
                        fieldId = R.string.address_state
                    )
                )
            }
        }
        return errors
    }
}

internal fun Map<PrimerInputFieldType, String?>.valueBy(type: PrimerInputFieldType): String {
    return this[type]?.sanitized().orEmpty()
}

package io.primer.android.ipay88.implementation.validation.rules

import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.domain.validation.ValidationRule
import io.primer.android.errors.data.exception.IllegalClientSessionValueException
import io.primer.android.ipay88.implementation.errors.data.exception.IPay88IllegalValueKey
import io.primer.android.ipay88.implementation.validation.IPay88ValidationData

internal class ValidCustomerFirstNameRule :
    ValidationRule<IPay88ValidationData> {
    override fun validate(t: IPay88ValidationData): ValidationResult {
        val firstName = t.clientSession?.clientSession?.customer?.firstName
        return when (firstName.isNullOrBlank().not()) {
            true -> ValidationResult.Success
            false ->
                ValidationResult.Failure(
                    IllegalClientSessionValueException(
                        IPay88IllegalValueKey.ILLEGAL_CUSTOMER_FIRST_NAME,
                        firstName,
                    ),
                )
        }
    }
}

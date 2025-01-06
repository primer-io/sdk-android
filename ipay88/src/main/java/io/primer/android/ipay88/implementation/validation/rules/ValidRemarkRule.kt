package io.primer.android.ipay88.implementation.validation.rules

import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.domain.validation.ValidationRule
import io.primer.android.errors.data.exception.IllegalClientSessionValueException
import io.primer.android.ipay88.implementation.errors.data.exception.IPay88IllegalValueKey
import io.primer.android.ipay88.implementation.validation.IPay88ValidationData

internal class ValidRemarkRule :
    ValidationRule<IPay88ValidationData> {
    override fun validate(t: IPay88ValidationData): ValidationResult {
        val remark = t.clientSession?.clientSession?.customerId
        return when (
            t.clientToken.actionType.isBlank() || remark.isNullOrBlank().not()
        ) {
            true -> ValidationResult.Success
            false ->
                ValidationResult.Failure(
                    IllegalClientSessionValueException(
                        IPay88IllegalValueKey.ILLEGAL_CUSTOMER_ID,
                        remark,
                    ),
                )
        }
    }
}

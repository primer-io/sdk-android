package io.primer.android.ipay88.implementation.validation.rules

import io.primer.android.ipay88.implementation.validation.IPay88ValidationData
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.domain.validation.ValidationRule
import io.primer.android.errors.data.exception.IllegalClientSessionValueException
import io.primer.android.ipay88.implementation.errors.data.exception.IPay88IllegalValueKey

internal class ValidClientSessionAmountRule : ValidationRule<IPay88ValidationData> {
    override fun validate(t: IPay88ValidationData): ValidationResult {
        val amount = t.clientSession?.clientSession?.totalAmount ?: 0
        return when (amount > 0) {
            true -> ValidationResult.Success
            false -> ValidationResult.Failure(
                IllegalClientSessionValueException(
                    IPay88IllegalValueKey.ILLEGAL_AMOUNT,
                    amount
                )
            )
        }
    }
}

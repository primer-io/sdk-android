package io.primer.android.ipay88.implementation.validation.rules

import io.primer.android.ipay88.implementation.validation.IPay88ValidationData
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.domain.validation.ValidationRule
import io.primer.android.errors.data.exception.IllegalClientSessionValueException
import io.primer.android.ipay88.implementation.errors.data.exception.IPay88IllegalValueKey

internal class ValidClientSessionCurrencyRule :
    ValidationRule<IPay88ValidationData> {
    override fun validate(t: IPay88ValidationData): ValidationResult {
        val currencyCode = t.clientSession?.clientSession?.currencyCode
        return when (currencyCode == t.clientToken.supportedCurrencyCode) {
            true -> ValidationResult.Success
            false -> ValidationResult.Failure(
                IllegalClientSessionValueException(
                    IPay88IllegalValueKey.ILLEGAL_CURRENCY_CODE,
                    currencyCode,
                    t.clientToken.supportedCurrencyCode
                )
            )
        }
    }
}

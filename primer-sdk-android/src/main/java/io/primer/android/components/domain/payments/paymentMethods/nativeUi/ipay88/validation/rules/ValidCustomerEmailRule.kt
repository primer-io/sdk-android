package io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.rules

import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.core.validation.ValidationRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.exception.IPay88IllegalValueKey
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.IPay88ValidationData
import io.primer.android.data.base.exceptions.IllegalClientSessionValueException

internal class ValidCustomerEmailRule :
    ValidationRule<IPay88ValidationData> {
    override fun validate(t: IPay88ValidationData): ValidationResult {
        val emailAddress = t.clientSession?.clientSession?.customer?.emailAddress
        return when (emailAddress.isNullOrBlank().not()) {
            true -> ValidationResult.Success
            false -> ValidationResult.Failure(
                IllegalClientSessionValueException(
                    IPay88IllegalValueKey.ILLEGAL_CUSTOMER_EMAIL,
                    emailAddress,
                )
            )
        }
    }
}

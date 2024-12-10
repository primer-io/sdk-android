package io.primer.android.components.validation.rules

import io.primer.android.components.domain.exception.UnsupportedPaymentMethodManagerException
import io.primer.android.components.implementation.domain.mapper.PrimerHeadlessUniversalCheckoutPaymentMethodMapper
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.domain.validation.ValidationRule

internal class ValidPaymentMethodManagerRule(
    private val paymentMethodManager: PrimerHeadlessUniversalCheckoutPaymentMethodMapper
) : ValidationRule<PaymentMethodManagerInitValidationData> {
    override fun validate(t: PaymentMethodManagerInitValidationData): ValidationResult {
        return paymentMethodManager.getPrimerHeadlessUniversalCheckoutPaymentMethod(
            t.paymentMethodType
        ).let {
            when (it.paymentMethodManagerCategories.contains(t.category)) {
                true -> ValidationResult.Success
                false -> ValidationResult.Failure(
                    UnsupportedPaymentMethodManagerException(t.paymentMethodType, t.category)
                )
            }
        }
    }
}

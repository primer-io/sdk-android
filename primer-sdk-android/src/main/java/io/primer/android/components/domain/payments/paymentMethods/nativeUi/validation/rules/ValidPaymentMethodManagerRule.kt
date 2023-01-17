package io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules

import io.primer.android.components.domain.core.mapper.PrimerHeadlessUniversalCheckoutPaymentMethodMapper
import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.core.validation.ValidationRule
import io.primer.android.components.domain.exception.UnsupportedPaymentMethodManagerException

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

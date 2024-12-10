package io.primer.android.components.validation.rules

import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.domain.validation.ValidationRule
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import io.primer.android.components.implementation.domain.PaymentMethodDescriptorsRepository

internal class ValidPaymentMethodRule(
    private val paymentMethodRepository: PaymentMethodDescriptorsRepository
) : ValidationRule<PaymentMethodManagerInitValidationData> {
    override fun validate(t: PaymentMethodManagerInitValidationData): ValidationResult {
        return paymentMethodRepository.getPaymentMethodDescriptors()
            .find { it.config.type == t.paymentMethodType }
            .let {
                if (it != null) {
                    ValidationResult.Success
                } else {
                    ValidationResult.Failure(UnsupportedPaymentMethodException(t.paymentMethodType))
                }
            }
    }
}

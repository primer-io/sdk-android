package io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules

import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.core.validation.ValidationRule
import io.primer.android.domain.exception.UnsupportedPaymentMethodException
import io.primer.android.domain.payments.methods.repository.PaymentMethodDescriptorsRepository

internal class ValidPaymentMethodRule(
    private val paymentMethodRepository: PaymentMethodDescriptorsRepository
) : ValidationRule<PaymentMethodManagerInitValidationData> {
    override fun validate(t: PaymentMethodManagerInitValidationData): ValidationResult {
        return paymentMethodRepository.getPaymentMethodDescriptors()
            .find { it.config.type == t.paymentMethodType }
            .let {
                if (it != null) ValidationResult.Success
                else ValidationResult.Failure(
                    UnsupportedPaymentMethodException(t.paymentMethodType)
                )
            }
    }
}

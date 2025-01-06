package io.primer.android.components.validation.rules

import io.primer.android.components.implementation.domain.mapper.PrimerHeadlessUniversalCheckoutPaymentMethodMapper
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.domain.validation.ValidationRule
import io.primer.android.domain.exception.UnsupportedPaymentIntentException

internal class ValidSessionIntentRule(
    private val paymentMethodManager: PrimerHeadlessUniversalCheckoutPaymentMethodMapper,
) : ValidationRule<PaymentMethodManagerSessionIntentValidationData> {
    override fun validate(t: PaymentMethodManagerSessionIntentValidationData): ValidationResult {
        return paymentMethodManager.getPrimerHeadlessUniversalCheckoutPaymentMethod(
            t.paymentMethodType,
        ).let {
            when (it.supportedPrimerSessionIntents.contains(t.sessionIntent)) {
                true -> ValidationResult.Success
                false ->
                    ValidationResult.Failure(
                        UnsupportedPaymentIntentException(t.paymentMethodType, t.sessionIntent),
                    )
            }
        }
    }
}

package io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules

import io.primer.android.components.domain.core.mapper.PrimerHeadlessUniversalCheckoutPaymentMethodMapper
import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.core.validation.ValidationRule
import io.primer.android.domain.exception.UnsupportedPaymentIntentException

internal class ValidSessionIntentRule(
    private val paymentMethodManager: PrimerHeadlessUniversalCheckoutPaymentMethodMapper
) : ValidationRule<PaymentMethodManagerSessionIntentValidationData> {
    override fun validate(t: PaymentMethodManagerSessionIntentValidationData):
        ValidationResult {
        return paymentMethodManager.getPrimerHeadlessUniversalCheckoutPaymentMethod(
            t.paymentMethodType
        ).let {
            when (it.supportedPrimerSessionIntents.contains(t.sessionIntent)) {
                true -> ValidationResult.Success
                false -> ValidationResult.Failure(
                    UnsupportedPaymentIntentException(t.paymentMethodType, t.sessionIntent)
                )
            }
        }
    }
}

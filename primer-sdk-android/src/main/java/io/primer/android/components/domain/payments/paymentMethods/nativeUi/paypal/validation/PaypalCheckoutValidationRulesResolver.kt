package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.validation

import io.primer.android.components.data.payments.paymentMethods.nativeUi.googlepay.exception.GooglePayIllegalValueKey
import io.primer.android.components.data.payments.paymentMethods.nativeUi.paypal.exception.PaypalIllegalValueKey
import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.core.validation.ValidationRule
import io.primer.android.components.domain.core.validation.ValidationRulesChain
import io.primer.android.components.domain.core.validation.ValidationRulesResolver
import io.primer.android.data.base.exceptions.IllegalValueException

internal class PaypalCheckoutValidationRulesResolver(
    private val orderTokenRule: PaypalValidOrderTokenRule
) : ValidationRulesResolver<String?> {
    override fun resolve() = ValidationRulesChain<String?>().addRule(orderTokenRule)
}

internal class PaypalValidOrderTokenRule : ValidationRule<String?> {
    override fun validate(t: String?): ValidationResult {
        return when (t != null) {
            true -> ValidationResult.Success
            false -> ValidationResult.Failure(
                IllegalValueException(
                    GooglePayIllegalValueKey.SDK_PAYMENT_DATA,
                    "Required value for ${PaypalIllegalValueKey.INTENT_CHECKOUT_TOKEN} " +
                        "was null."
                )
            )
        }
    }
}

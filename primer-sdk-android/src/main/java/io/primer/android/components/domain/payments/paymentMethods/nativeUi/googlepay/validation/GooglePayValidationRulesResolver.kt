package io.primer.android.components.domain.payments.paymentMethods.nativeUi.googlepay.validation

import com.google.android.gms.wallet.PaymentData
import io.primer.android.components.data.payments.paymentMethods.nativeUi.googlepay.exception.GooglePayIllegalValueKey
import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.core.validation.ValidationRule
import io.primer.android.components.domain.core.validation.ValidationRulesChain
import io.primer.android.components.domain.core.validation.ValidationRulesResolver
import io.primer.android.data.base.exceptions.IllegalValueException

internal class GooglePayValidationRulesResolver(
    private val validPaymentDataMethodRule: GooglePayValidPaymentDataMethodRule
) : ValidationRulesResolver<PaymentData?> {
    override fun resolve() = ValidationRulesChain<PaymentData?>().addRule(
        validPaymentDataMethodRule
    )
}

internal class GooglePayValidPaymentDataMethodRule : ValidationRule<PaymentData?> {
    override fun validate(t: PaymentData?): ValidationResult {
        return when (t != null) {
            true -> ValidationResult.Success
            false -> ValidationResult.Failure(
                IllegalValueException(
                    GooglePayIllegalValueKey.SDK_PAYMENT_DATA,
                    "Required value for ${GooglePayIllegalValueKey.SDK_PAYMENT_DATA} " +
                        "was null."
                )
            )
        }
    }
}

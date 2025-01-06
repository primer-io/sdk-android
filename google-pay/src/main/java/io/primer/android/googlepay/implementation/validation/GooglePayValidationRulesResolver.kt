package io.primer.android.googlepay.implementation.validation

import com.google.android.gms.wallet.PaymentData
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.domain.validation.ValidationRule
import io.primer.android.core.domain.validation.ValidationRulesChain
import io.primer.android.core.domain.validation.ValidationRulesResolver
import io.primer.android.googlepay.implementation.errors.data.exception.GooglePayIllegalValueKey

internal class GooglePayValidationRulesResolver(
    private val validPaymentDataMethodRule: GooglePayValidPaymentDataMethodRule,
) : ValidationRulesResolver<PaymentData?> {
    override fun resolve() =
        ValidationRulesChain<PaymentData?>().addRule(
            validPaymentDataMethodRule,
        )
}

internal class GooglePayValidPaymentDataMethodRule : ValidationRule<PaymentData?> {
    override fun validate(t: PaymentData?): ValidationResult {
        return when (t != null) {
            true -> ValidationResult.Success
            false ->
                ValidationResult.Failure(
                    io.primer.android.errors.data.exception.IllegalValueException(
                        GooglePayIllegalValueKey.SDK_PAYMENT_DATA,
                        "Required value for ${GooglePayIllegalValueKey.SDK_PAYMENT_DATA} " +
                            "was null.",
                    ),
                )
        }
    }
}

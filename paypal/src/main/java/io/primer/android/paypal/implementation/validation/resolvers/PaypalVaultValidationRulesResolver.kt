package io.primer.android.paypal.implementation.validation.resolvers

import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.domain.validation.ValidationRule
import io.primer.android.core.domain.validation.ValidationRulesChain
import io.primer.android.core.domain.validation.ValidationRulesResolver
import io.primer.android.paypal.implementation.errors.data.exception.PaypalIllegalValueKey

internal class PaypalVaultValidationRulesResolver(
    private val billingAgreementTokenRule: PaypalValidBillingAgreementTokenRule,
) : ValidationRulesResolver<String?> {
    override fun resolve() = ValidationRulesChain<String?>().addRule(billingAgreementTokenRule)
}

internal class PaypalValidBillingAgreementTokenRule : ValidationRule<String?> {
    override fun validate(t: String?): ValidationResult {
        return when (t != null) {
            true -> ValidationResult.Success
            false ->
                ValidationResult.Failure(
                    io.primer.android.errors.data.exception.IllegalValueException(
                        PaypalIllegalValueKey.INTENT_VAULT_TOKEN,
                        "Required value for ${PaypalIllegalValueKey.INTENT_VAULT_TOKEN} " +
                            "was null.",
                    ),
                )
        }
    }
}

package io.primer.android.paypal.implementation.validation.resolvers

import io.primer.android.paypal.implementation.errors.domain.exception.PaypalInvalidValueKey
import io.primer.android.paypal.implementation.tokenization.domain.model.PaypalCreateOrderParams
import io.primer.android.core.domain.validation.ValidationResult
import io.primer.android.core.domain.validation.ValidationRule
import io.primer.android.core.domain.validation.ValidationRulesChain
import io.primer.android.core.domain.validation.ValidationRulesResolver
import java.util.Currency

internal class PaypalCheckoutOrderValidationRulesResolver(
    private val validOrderAmountRule: PaypalValidOrderAmountRule,
    private val validOrderCurrencyRule: PaypalValidOrderCurrencyRule
) : ValidationRulesResolver<PaypalCreateOrderParams> {
    override fun resolve() = ValidationRulesChain<PaypalCreateOrderParams>()
        .addRule(validOrderAmountRule)
        .addRule(validOrderCurrencyRule)
}

internal class PaypalValidOrderAmountRule : ValidationRule<PaypalCreateOrderParams> {
    override fun validate(t: PaypalCreateOrderParams): ValidationResult {
        return when ((t.amount ?: 0) > 0) {
            true -> ValidationResult.Success
            false -> ValidationResult.Failure(
                io.primer.android.errors.data.exception.IllegalValueException(
                    PaypalInvalidValueKey.ILLEGAL_AMOUNT,
                    "Required value for ${PaypalInvalidValueKey.ILLEGAL_AMOUNT.key} " +
                        "must be greater than 0."
                )
            )
        }
    }
}

internal class PaypalValidOrderCurrencyRule : ValidationRule<PaypalCreateOrderParams> {
    override fun validate(t: PaypalCreateOrderParams): ValidationResult {
        return try {
            Currency.getInstance(t.currencyCode)
            ValidationResult.Success
        } catch (e: IllegalArgumentException) {
            ValidationResult.Failure(
                io.primer.android.errors.data.exception.IllegalValueException(
                    PaypalInvalidValueKey.ILLEGAL_CURRENCY_CODE,
                    e.message
                )
            )
        }
    }
}

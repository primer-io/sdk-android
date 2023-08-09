package io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.validation

import io.primer.android.components.domain.core.validation.ValidationResult
import io.primer.android.components.domain.core.validation.ValidationRule
import io.primer.android.components.domain.core.validation.ValidationRulesChain
import io.primer.android.components.domain.core.validation.ValidationRulesResolver
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.exception.PaypalInvalidValueKey
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.paypal.models.PaypalCreateOrderParams
import io.primer.android.data.base.exceptions.IllegalValueException
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
                IllegalValueException(
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
                IllegalValueException(
                    PaypalInvalidValueKey.ILLEGAL_CURRENCY_CODE,
                    e.message
                )
            )
        }
    }
}

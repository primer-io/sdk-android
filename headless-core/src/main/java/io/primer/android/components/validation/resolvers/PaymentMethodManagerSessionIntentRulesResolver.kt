package io.primer.android.components.validation.resolvers

import io.primer.android.components.validation.rules.PaymentMethodManagerSessionIntentValidationData
import io.primer.android.components.validation.rules.ValidSessionIntentRule
import io.primer.android.core.domain.validation.ValidationRulesChain
import io.primer.android.core.domain.validation.ValidationRulesResolver

internal class PaymentMethodManagerSessionIntentRulesResolver(
    private val validSessionIntentRule: ValidSessionIntentRule,
) : ValidationRulesResolver<PaymentMethodManagerSessionIntentValidationData> {
    override fun resolve() =
        ValidationRulesChain<PaymentMethodManagerSessionIntentValidationData>()
            .addRule(validSessionIntentRule)
}

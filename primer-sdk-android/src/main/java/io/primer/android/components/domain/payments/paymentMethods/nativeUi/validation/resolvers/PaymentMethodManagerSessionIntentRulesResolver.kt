package io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.resolvers

import io.primer.android.components.domain.core.validation.ValidationRulesChain
import io.primer.android.components.domain.core.validation.ValidationRulesResolver
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules.PaymentMethodManagerSessionIntentValidationData
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules.ValidSessionIntentRule

internal class PaymentMethodManagerSessionIntentRulesResolver(
    private val validSessionIntentRule: ValidSessionIntentRule
) : ValidationRulesResolver<PaymentMethodManagerSessionIntentValidationData> {
    override fun resolve() =
        ValidationRulesChain<PaymentMethodManagerSessionIntentValidationData>()
            .addRule(validSessionIntentRule)
}

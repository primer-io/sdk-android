package io.primer.android.components.validation.resolvers

import io.primer.android.components.assets.validation.SdkInitializedRule
import io.primer.android.components.validation.rules.PaymentMethodManagerInitValidationData
import io.primer.android.components.validation.rules.ValidPaymentMethodManagerRule
import io.primer.android.components.validation.rules.ValidPaymentMethodRule
import io.primer.android.core.domain.validation.ValidationRulesChain
import io.primer.android.core.domain.validation.ValidationRulesResolver

internal class PaymentMethodManagerInitValidationRulesResolver(
    private val sdkInitializedRule: SdkInitializedRule,
    private val validPaymentMethodManagerRule: ValidPaymentMethodManagerRule,
    private val validPaymentMethodRule: ValidPaymentMethodRule,
) : ValidationRulesResolver<PaymentMethodManagerInitValidationData> {
    override fun resolve() =
        ValidationRulesChain<PaymentMethodManagerInitValidationData>()
            .addRule(sdkInitializedRule)
            .addRule(validPaymentMethodManagerRule)
            .addRule(validPaymentMethodRule)
}

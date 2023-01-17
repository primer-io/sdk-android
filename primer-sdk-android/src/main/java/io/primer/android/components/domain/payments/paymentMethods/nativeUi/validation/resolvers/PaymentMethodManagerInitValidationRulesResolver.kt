package io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.resolvers

import io.primer.android.components.domain.core.validation.ValidationRulesChain
import io.primer.android.components.domain.core.validation.ValidationRulesResolver
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules.PaymentMethodManagerInitValidationData
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules.SdkInitializedRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules.ValidPaymentMethodManagerRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules.ValidPaymentMethodRule

internal class PaymentMethodManagerInitValidationRulesResolver(
    private val sdkInitializedRule: SdkInitializedRule,
    private val validPaymentMethodManagerRule: ValidPaymentMethodManagerRule,
    private val validPaymentMethodRule: ValidPaymentMethodRule
) : ValidationRulesResolver<PaymentMethodManagerInitValidationData> {
    override fun resolve() = ValidationRulesChain<PaymentMethodManagerInitValidationData>()
        .addRule(sdkInitializedRule)
        .addRule(validPaymentMethodManagerRule)
        .addRule(validPaymentMethodRule)
}

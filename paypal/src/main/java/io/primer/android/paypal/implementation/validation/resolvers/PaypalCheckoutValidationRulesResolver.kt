package io.primer.android.paypal.implementation.validation.resolvers

import io.primer.android.core.domain.validation.ValidationRulesChain
import io.primer.android.core.domain.validation.ValidationRulesResolver

internal class PaypalCheckoutValidationRulesResolver(
    private val orderTokenRule: PaypalValidOrderTokenRule
) : ValidationRulesResolver<String?> {
    override fun resolve() = ValidationRulesChain<String?>().addRule(orderTokenRule)
}

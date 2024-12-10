package io.primer.android.ipay88.implementation.validation.resolvers

import io.primer.android.ipay88.implementation.validation.IPay88ValidationData
import io.primer.android.ipay88.implementation.validation.rules.ValidClientSessionAmountRule
import io.primer.android.ipay88.implementation.validation.rules.ValidClientSessionCountryCodeRule
import io.primer.android.ipay88.implementation.validation.rules.ValidClientSessionCurrencyRule
import io.primer.android.ipay88.implementation.validation.rules.ValidCustomerEmailRule
import io.primer.android.ipay88.implementation.validation.rules.ValidCustomerFirstNameRule
import io.primer.android.ipay88.implementation.validation.rules.ValidCustomerLastNameRule
import io.primer.android.ipay88.implementation.validation.rules.ValidProductDescriptionRule
import io.primer.android.ipay88.implementation.validation.rules.ValidRemarkRule
import io.primer.android.core.domain.validation.ValidationRulesChain
import io.primer.android.core.domain.validation.ValidationRulesResolver

internal class IPay88ValidationRulesResolver(
    private val clientSessionAmountRule: ValidClientSessionAmountRule,
    private val clientSessionCurrencyRule: ValidClientSessionCurrencyRule,
    private val clientSessionCountryCodeRule: ValidClientSessionCountryCodeRule,
    private val productDescriptionRule: ValidProductDescriptionRule,
    private val customerFirstNameRule: ValidCustomerFirstNameRule,
    private val customerLastNameRule: ValidCustomerLastNameRule,
    private val customerEmailRule: ValidCustomerEmailRule,
    private val validRemarkRule: ValidRemarkRule
) : ValidationRulesResolver<IPay88ValidationData> {

    override fun resolve() = ValidationRulesChain<IPay88ValidationData>()
        .addRule(clientSessionAmountRule)
        .addRule(clientSessionCurrencyRule)
        .addRule(clientSessionCountryCodeRule)
        .addRule(productDescriptionRule)
        .addRule(customerFirstNameRule)
        .addRule(customerLastNameRule)
        .addRule(customerEmailRule)
        .addRule(validRemarkRule)
}

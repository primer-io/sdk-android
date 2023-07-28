package io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.resolvers

import io.primer.android.components.domain.core.validation.ValidationRulesChain
import io.primer.android.components.domain.core.validation.ValidationRulesResolver
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.IPay88ValidationData
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.rules.ValidClientSessionAmountRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.rules.ValidClientSessionCountryCodeRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.rules.ValidClientSessionCurrencyRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.rules.ValidCustomerEmailRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.rules.ValidCustomerFirstNameRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.rules.ValidCustomerLastNameRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.rules.ValidProductDescriptionRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.ipay88.validation.rules.ValidRemarkRule

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

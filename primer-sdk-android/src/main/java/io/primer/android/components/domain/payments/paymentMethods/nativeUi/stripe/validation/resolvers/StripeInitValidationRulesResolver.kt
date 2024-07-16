package io.primer.android.components.domain.payments.paymentMethods.nativeUi.stripe.validation.resolvers

import io.primer.android.components.domain.core.validation.ValidationRulesChain
import io.primer.android.components.domain.core.validation.ValidationRulesResolver
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.stripe.validation.rules.ValidStripeMandateDataRule
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.stripe.validation.rules.ValidStripePublishableKeyRule
import io.primer.android.data.settings.PrimerStripeOptions

internal class StripeInitValidationRulesResolver(
    private val validStripePublishableKeyRule: ValidStripePublishableKeyRule,
    private val validStripeMandateDataRule: ValidStripeMandateDataRule
) : ValidationRulesResolver<PrimerStripeOptions> {

    override fun resolve() = ValidationRulesChain<PrimerStripeOptions>()
        .addRule(validStripePublishableKeyRule)
        .addRule(validStripeMandateDataRule)
}

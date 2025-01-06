package io.primer.android.stripe.ach.implementation.validation.resolvers

import io.primer.android.core.domain.validation.ValidationRulesChain
import io.primer.android.core.domain.validation.ValidationRulesResolver
import io.primer.android.data.settings.PrimerStripeOptions
import io.primer.android.stripe.ach.implementation.validation.rules.ValidStripeMandateDataRule
import io.primer.android.stripe.ach.implementation.validation.rules.ValidStripePublishableKeyRule

class StripeInitValidationRulesResolver internal constructor(
    private val validStripePublishableKeyRule: ValidStripePublishableKeyRule,
    private val validStripeMandateDataRule: ValidStripeMandateDataRule,
) : ValidationRulesResolver<PrimerStripeOptions> {
    override fun resolve() =
        ValidationRulesChain<PrimerStripeOptions>()
            .addRule(validStripePublishableKeyRule)
            .addRule(validStripeMandateDataRule)
}

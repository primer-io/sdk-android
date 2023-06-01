package io.primer.android.components.domain.payments.vault.validation.resolvers

import io.primer.android.components.domain.core.validation.ValidationRulesChain
import io.primer.android.components.domain.core.validation.ValidationRulesResolver
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules.SdkInitializedRule
import io.primer.android.components.domain.payments.vault.validation.rules.ValidClientSessionCustomerIdRule

internal class VaultManagerInitValidationRulesResolver(
    private val sdkInitializedRule: SdkInitializedRule,
    private val validClientSessionCustomerIdRule: ValidClientSessionCustomerIdRule
) : ValidationRulesResolver<Unit> {

    override fun resolve(): ValidationRulesChain<Unit> {
        return ValidationRulesChain<Unit>()
            .addRule(sdkInitializedRule)
            .addRule(validClientSessionCustomerIdRule)
    }
}

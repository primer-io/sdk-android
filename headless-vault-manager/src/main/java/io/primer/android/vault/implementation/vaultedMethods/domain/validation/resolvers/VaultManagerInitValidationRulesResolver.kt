package io.primer.android.vault.implementation.vaultedMethods.domain.validation.resolvers

import io.primer.android.core.domain.validation.ValidationRulesChain
import io.primer.android.core.domain.validation.ValidationRulesResolver
import io.primer.android.vault.implementation.vaultedMethods.domain.validation.rules.SdkInitializedRule
import io.primer.android.vault.implementation.vaultedMethods.domain.validation.rules.ValidClientSessionCustomerIdRule

internal class VaultManagerInitValidationRulesResolver(
    private val sdkInitializedRule: SdkInitializedRule,
    private val validClientSessionCustomerIdRule: ValidClientSessionCustomerIdRule,
) : ValidationRulesResolver<Unit> {
    override fun resolve(): ValidationRulesChain<Unit> {
        return ValidationRulesChain<Unit>()
            .addRule(sdkInitializedRule)
            .addRule(validClientSessionCustomerIdRule)
    }
}

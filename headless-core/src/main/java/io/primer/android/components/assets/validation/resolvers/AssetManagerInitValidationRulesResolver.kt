package io.primer.android.components.assets.validation.resolvers

import io.primer.android.components.assets.validation.SdkInitializedRule
import io.primer.android.core.domain.validation.ValidationRulesChain
import io.primer.android.core.domain.validation.ValidationRulesResolver

internal class AssetManagerInitValidationRulesResolver(
    private val sdkInitializedRule: SdkInitializedRule
) : ValidationRulesResolver<Any> {
    override fun resolve() = ValidationRulesChain<Any>()
        .addRule(sdkInitializedRule)
}

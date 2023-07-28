package io.primer.android.components.domain.assets.validation.resolvers

import io.primer.android.components.domain.core.validation.ValidationRulesChain
import io.primer.android.components.domain.core.validation.ValidationRulesResolver
import io.primer.android.components.domain.payments.paymentMethods.nativeUi.validation.rules.SdkInitializedRule

internal class AssetManagerInitValidationRulesResolver(
    private val sdkInitializedRule: SdkInitializedRule,
) : ValidationRulesResolver<Any> {
    override fun resolve() = ValidationRulesChain<Any>()
        .addRule(sdkInitializedRule)
}

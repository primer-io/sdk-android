package io.primer.android.components.domain.core.validation

internal interface ValidationRulesResolver<T> {

    fun resolve(): ValidationRulesChain<T>
}

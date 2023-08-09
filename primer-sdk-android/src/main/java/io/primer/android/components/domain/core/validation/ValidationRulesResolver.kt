package io.primer.android.components.domain.core.validation

internal fun interface ValidationRulesResolver<T> {

    fun resolve(): ValidationRulesChain<T>
}

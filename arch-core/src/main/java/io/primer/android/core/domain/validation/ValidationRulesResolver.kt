package io.primer.android.core.domain.validation

fun interface ValidationRulesResolver<T> {
    fun resolve(): ValidationRulesChain<T>
}

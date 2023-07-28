package io.primer.android.components.domain.core.validation

internal class ValidationRulesChain<T> {

    private val _rules: MutableList<ValidationRule<T>> = mutableListOf()
    val rules: List<ValidationRule<T>> = _rules

    fun addRule(rule: ValidationRule<T>) = apply {
        _rules.add(rule)
    }
}

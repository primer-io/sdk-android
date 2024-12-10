package io.primer.android.core.domain.validation

class ValidationRulesChain<T> {

    private val _rules: MutableList<ValidationRule<T>> = mutableListOf()
    val rules: List<ValidationRule<T>> = _rules

    fun addRule(rule: ValidationRule<T>) = apply {
        _rules.add(rule)
    }
}

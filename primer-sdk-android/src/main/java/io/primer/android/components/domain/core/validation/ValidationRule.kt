package io.primer.android.components.domain.core.validation

internal interface ValidationRule<in T> {

    fun validate(t: T): ValidationResult
}

package io.primer.android.core.domain.validation

fun interface ValidationRule<in T> {

    fun validate(t: T): ValidationResult
}

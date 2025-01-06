package io.primer.android.core.domain.validation

sealed class ValidationResult {
    object Success : ValidationResult()

    data class Failure(val exception: Exception) : ValidationResult()
}

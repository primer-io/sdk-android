package io.primer.android.components.domain.core.validation

internal sealed class ValidationResult {
    object Success : ValidationResult()
    class Failure(val exception: Exception) : ValidationResult()
}

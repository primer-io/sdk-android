package io.primer.components.domain.models

data class PaymentValidationState(
    val isValid: Boolean = false,
    val errors: List<ValidationError> = emptyList(),
)

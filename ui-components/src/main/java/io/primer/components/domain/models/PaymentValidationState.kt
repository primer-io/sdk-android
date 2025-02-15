package io.primer.components.domain.models

/**
 * A data class representing the validation state of payment method data supplied during the checkout process.
 * It includes information on whether the provided data is valid and any associated validation errors.
 *
 * @property isValid Indicates whether the provided input is valid.
 * @property errors A list of [ValidationError] objects that represent any validation issues encountered.
 */
data class PaymentValidationState(
    val isValid: Boolean = false,
    val errors: List<ValidationError> = emptyList(),
)

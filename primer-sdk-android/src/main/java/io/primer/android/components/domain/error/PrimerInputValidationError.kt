package io.primer.android.components.domain.error

import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import io.primer.android.components.PrimerHeadlessUniversalCheckout
import java.util.UUID

/**
 * Represents a validation error in the [PrimerHeadlessUniversalCheckout], including an error identifier,
 * a description, and the type of input element associated with the error.
 *
 * @property errorId A unique identifier for the validation error.
 * @property description A human-readable description of the validation error.
 * @property inputElementType The type of input element associated with the validation error.
 * @property diagnosticsId A unique identifier for diagnostic purposes.
 */
data class PrimerInputValidationError(
    val errorId: String,
    val description: String,
    val inputElementType: PrimerInputElementType
) {
    val diagnosticsId: String = UUID.randomUUID().toString()
}

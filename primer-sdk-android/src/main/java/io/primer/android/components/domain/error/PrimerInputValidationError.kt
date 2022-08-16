package io.primer.android.components.domain.error

import io.primer.android.components.domain.inputs.models.PrimerInputElementType

data class PrimerInputValidationError(
    val errorId: String,
    val description: String,
    val inputElementType: PrimerInputElementType
)

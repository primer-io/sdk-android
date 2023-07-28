package io.primer.android.components.domain.error

import io.primer.android.components.domain.inputs.models.PrimerInputElementType
import java.util.UUID

data class PrimerInputValidationError(
    val errorId: String,
    val description: String,
    val inputElementType: PrimerInputElementType,
    val diagnosticsId: String = UUID.randomUUID().toString(),
)

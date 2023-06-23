package io.primer.android.components.domain.error

import java.util.UUID

data class PrimerValidationError(
    val errorId: String,
    val description: String,
    val diagnosticsId: String = UUID.randomUUID().toString(),
)
